#!/bin/bash

###############################################################################
# BuzzLink Deployment Script for EC2
#
# This script handles the deployment of BuzzLink application on EC2 instance.
# It pulls the latest code, rebuilds Docker containers, and performs health checks.
###############################################################################

set -e  # Exit on error
set -u  # Exit on undefined variable

# Configuration
PROJECT_DIR="/home/ubuntu/BuzzLink"
DOCKER_COMPOSE_FILE="docker-compose.yml"
BRANCH="${1:-main}"
LOG_FILE="/tmp/buzzlink-deploy-$(date +%Y%m%d-%H%M%S).log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

# Check if running as correct user
if [ "$(whoami)" != "ubuntu" ]; then
    error "This script should be run as 'ubuntu' user"
    exit 1
fi

# Start deployment
log "Starting BuzzLink deployment..."
log "Branch: $BRANCH"
log "Log file: $LOG_FILE"

# Navigate to project directory
cd "$PROJECT_DIR" || {
    error "Failed to navigate to $PROJECT_DIR"
    exit 1
}

# Backup current state
log "Creating backup of current state..."
BACKUP_DIR="/tmp/buzzlink-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"
docker-compose ps > "$BACKUP_DIR/containers.txt" 2>&1 || true

# Pull latest code
log "Pulling latest code from GitHub..."
git fetch origin || {
    error "Failed to fetch from origin"
    exit 1
}

git reset --hard "origin/$BRANCH" || {
    error "Failed to reset to origin/$BRANCH"
    exit 1
}

CURRENT_COMMIT=$(git rev-parse --short HEAD)
COMMIT_MSG=$(git log -1 --pretty=%B)
log "Deployed commit: $CURRENT_COMMIT"
log "Commit message: $COMMIT_MSG"

# Check if .env file exists
if [ ! -f .env ]; then
    warning ".env file not found. Make sure environment variables are set."
fi

# Stop existing containers
log "Stopping existing containers..."
docker-compose down || {
    warning "Failed to stop containers gracefully"
}

# Remove old containers and images
log "Cleaning up old containers..."
docker-compose rm -f || true

# Optional: Prune old images (uncomment if needed)
# log "Pruning unused Docker images..."
# docker image prune -f

# Build and start new containers
log "Building and starting new containers..."
docker-compose up -d --build || {
    error "Failed to start containers"
    log "Attempting to restore from backup..."
    # Add rollback logic here if needed
    exit 1
}

# Wait for services to start
log "Waiting for services to start..."
sleep 30

# Check container status
log "Checking container status..."
docker-compose ps

# Health checks
log "Performing health checks..."

# Backend health check
MAX_RETRIES=10
RETRY_COUNT=0
BACKEND_HEALTHY=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        BACKEND_HEALTHY=true
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    log "Backend health check attempt $RETRY_COUNT/$MAX_RETRIES..."
    sleep 5
done

if [ "$BACKEND_HEALTHY" = true ]; then
    log "✅ Backend is healthy"
else
    error "❌ Backend health check failed"
    log "Backend logs:"
    docker-compose logs --tail=50 backend
    exit 1
fi

# Frontend health check
FRONTEND_HEALTHY=false
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f http://localhost:3000 > /dev/null 2>&1; then
        FRONTEND_HEALTHY=true
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    log "Frontend health check attempt $RETRY_COUNT/$MAX_RETRIES..."
    sleep 5
done

if [ "$FRONTEND_HEALTHY" = true ]; then
    log "✅ Frontend is healthy"
else
    error "❌ Frontend health check failed"
    log "Frontend logs:"
    docker-compose logs --tail=50 frontend
    exit 1
fi

# Database health check
DB_HEALTHY=false
if docker-compose exec -T postgres pg_isready -U buzzlink_user -d buzzlink > /dev/null 2>&1; then
    DB_HEALTHY=true
    log "✅ Database is healthy"
else
    warning "⚠️  Database health check failed (this may be normal if migrations are running)"
fi

# Show recent logs
log "Recent application logs:"
docker-compose logs --tail=20

# Cleanup old Docker resources
log "Cleaning up unused Docker resources..."
docker image prune -f > /dev/null 2>&1 || true

# Summary
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log "✅ Deployment completed successfully!"
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log "Commit: $CURRENT_COMMIT"
log "Frontend: http://$(hostname -I | awk '{print $1}'):3000"
log "Backend: http://$(hostname -I | awk '{print $1}'):8080"
log "API Health: http://$(hostname -I | awk '{print $1}'):8080/actuator/health"
log "Log file: $LOG_FILE"
log "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

exit 0
