# BuzzLink - Enterprise Chat Application

A demonstration of an enterprise-grade chat application built with modern technologies and architecture patterns.

## Project Overview

BuzzLink is a class project demonstrating key enterprise software concepts including real-time communication, microservices architecture, authentication, monitoring, and CI/CD pipelines.

## Tech Stack

### Implemented & Working

| Component | Technology | Status |
|-----------|-----------|--------|
| Backend | Spring Boot 3.2 + Java 17 | Implemented |
| Frontend | Next.js 14 + TypeScript | Implemented |
| Authentication | Clerk (SSO + 2FA) | Implemented |
| Database | PostgreSQL / H2 | Implemented |
| Real-time | WebSocket (STOMP) | Implemented |
| Styling | Tailwind CSS | ✅ Implemented |

### Designed (Not Fully Running)

| Component | Technology | Status |
|-----------|-----------|--------|
| Messaging/Events | Apache Kafka | 📋 Design Only |
| Monitoring | Prometheus + Grafana | 📋 Design Only |
| Analytics | Apache Superset | 📋 Design Only |
| CI/CD | Jenkins | 📋 Pipeline Definition |
| Infrastructure | Terraform (AWS) | ✅ Implemented |

## 🚀 Deployment Options

### Option 1: AWS Deployment (Recommended - Fully Automated)

Deploy to AWS EC2 with one command using Terraform:

```bash
# Configure and deploy
./scripts/setup-terraform.sh
./scripts/terraform-deploy.sh
```

**See:** [DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md) for step-by-step guide

**Features:**
- ✅ Fully automated infrastructure provisioning
- ✅ Docker-based deployment
- ✅ Auto-scaling ready
- ✅ Production-ready security
- ✅ Free tier compatible (~$0/month for 12 months)

### Option 2: Local Development

## Quick Start (Local)

### Prerequisites

- **Java 17+**
- **Node.js 18+**
- **PostgreSQL** (or use H2 for development)
- **npm or yarn**

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/buzzlink.git
cd buzzlink
```

### 2. Start the Backend

```bash
cd backend

# Option A: Run with H2 (in-memory database)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Option B: Run with PostgreSQL
# First, start PostgreSQL:
# docker run --name buzzlink-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=buzzlink -p 5432:5432 -d postgres:15

./gradlew bootRun
```

Backend will be available at `http://localhost:8080`

### 3. Start the Frontend

```bash
cd frontend

# Install dependencies
npm install

# Copy environment file
cp .env.local.example .env.local

# Edit .env.local and add your Clerk keys from https://dashboard.clerk.com

# Start development server
npm run dev
```

Frontend will be available at `http://localhost:3000`

### 4. Set Up Clerk

1. Create a free account at [Clerk.com](https://clerk.com)
2. Create a new application
3. Copy the publishable and secret keys to `frontend/.env.local`
4. Enable email/password authentication in Clerk dashboard

### 5. Make a User Admin (Optional)

```bash
curl -X POST http://localhost:8080/api/users/make-admin \
  -H "Content-Type: application/json" \
  -d '{"clerkId": "user_YOUR_CLERK_ID", "isAdmin": true}'
```

Replace `user_YOUR_CLERK_ID` with your actual Clerk user ID (visible in the profile page after login).

## Project Structure

```
buzzlink/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/buzzlink/
│   │       ├── entity/      # JPA entities
│   │       ├── repository/  # Data access
│   │       ├── service/     # Business logic
│   │       ├── controller/  # REST endpoints
│   │       ├── websocket/   # WebSocket handlers
│   │       └── config/      # Configuration
│   └── build.gradle         # Dependencies
│
├── frontend/                # Next.js frontend
│   ├── src/
│   │   ├── app/            # Next.js pages (App Router)
│   │   ├── components/     # React components
│   │   ├── hooks/          # Custom hooks
│   │   ├── lib/            # API clients
│   │   └── types/          # TypeScript types
│   └── package.json
│
├── docs/                    # Documentation
│   ├── ARCHITECTURE.md     # System architecture
│   ├── API.md              # API documentation
│   ├── BI_ANALYTICS.md     # Superset design
│   └── MONITORING.md       # Prometheus design
│
├── Jenkinsfile             # CI/CD pipeline
└── README.md               # This file
```

## API Endpoints

### REST API

- `GET /api/channels` - List all channels
- `GET /api/channels/{id}/messages` - Get channel messages
- `DELETE /api/messages/{id}` - Delete message (admin)
- `POST /api/messages/{id}/reactions` - Toggle reaction
- `POST /api/users/sync` - Sync user from Clerk
- `GET /api/users/me` - Get current user
- `PUT /api/users/me` - Update profile

### WebSocket

Connect to `ws://localhost:8080/ws`

- Send to `/app/chat.sendMessage` - Send message
- Send to `/app/chat.typing` - Typing indicator
- Subscribe to `/topic/channel.{id}` - Receive messages
- Subscribe to `/topic/channel.{id}.typing` - Typing events
- Subscribe to `/topic/channel.{id}.presence` - Presence updates

See [docs/API.md](docs/API.md) for detailed documentation.

## Architecture

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Next.js   │◄───────►│ Spring Boot │◄───────►│ PostgreSQL  │
│   Frontend  │  REST   │   Backend   │   JPA   │  Database   │
│             │  + WS   │             │         │             │
└─────────────┘         └─────────────┘         └─────────────┘
      │                       │
      │                       │
      ▼                       ▼
┌─────────────┐         ┌─────────────┐
│    Clerk    │         │    Kafka    │
│  Auth/SSO   │         │   (Design)  │
└─────────────┘         └─────────────┘
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed architecture.

## Demo Features for Class Presentation

### Working Demo

1. **Sign up/Sign in** - Show Clerk authentication
2. **Real-time chat** - Send messages, see instant updates
3. **Typing indicators** - Type in one browser, see indicator in another
4. **Presence** - Show online user count
5. **Reactions** - Click 👍 to add reactions
6. **File links** - Share a file URL
7. **Admin delete** - Make yourself admin and delete a message
8. **Profile** - Update display name

### Architecture Slides

1. **System diagram** - Show components and data flow
2. **Tech stack** - Explain technology choices
3. **Kafka design** - How notifications would work at scale
4. **Prometheus** - Monitoring and metrics strategy
5. **Superset** - Analytics and BI dashboards
6. **Jenkins** - CI/CD pipeline walkthrough

## Testing

### Backend Tests

```bash
cd backend
./gradlew test
```

### Frontend Tests

```bash
cd frontend
npm test
```

## Building for Production

### Backend

```bash
cd backend
./gradlew build
java -jar build/libs/buzzlink-backend-1.0.0.jar
```

### Frontend

```bash
cd frontend
npm run build
npm start
```

## Design-Only Components

These components are designed but not fully implemented for the demo:

### Kafka (Message Streaming)

- **Purpose**: Decouple real-time delivery from event processing
- **Topics**: `buzzlink.messages.new`, `buzzlink.mentions`, `buzzlink.reactions`
- **Consumers**: Email service, push notifications, analytics pipeline
- **Implementation**: Stub in `NotificationService.java`

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for integration details.

### Prometheus (Monitoring)

- **Purpose**: Application metrics and alerting
- **Metrics**: Request rates, response times, WebSocket connections, JVM stats
- **Dashboards**: System health, application metrics, business KPIs
- **Endpoints**: `/actuator/prometheus` (already exposed)

See [docs/MONITORING.md](docs/MONITORING.md) for configuration.

### Apache Superset (Analytics)

- **Purpose**: Business intelligence dashboards
- **Dashboards**: Daily active users, channel analytics, engagement metrics
- **Data Source**: PostgreSQL (read-only replica)
- **Queries**: SQL views for common analytics

See [docs/BI_ANALYTICS.md](docs/BI_ANALYTICS.md) for dashboard designs.

### Jenkins (CI/CD)

- **Purpose**: Automated build, test, and deployment
- **Stages**: Build, test, security scan, Docker build, deploy
- **Environments**: Staging (auto), Production (manual approval)
- **Definition**: See [Jenkinsfile](Jenkinsfile)

## Troubleshooting

### Backend won't start

- Check Java version: `java -version` (need 17+)
- Check if port 8080 is available
- For PostgreSQL connection errors, verify database is running

### Frontend won't start

- Check Node version: `node -v` (need 18+)
- Verify `.env.local` has Clerk keys
- Clear Next.js cache: `rm -rf .next`

### WebSocket not connecting

- Verify backend is running on port 8080
- Check browser console for CORS errors
- Ensure SockJS library is loaded

### Messages not appearing

- Check browser console for errors
- Verify WebSocket connection status
- Check backend logs for exceptions

## Credits

- **Student**: [Your Name]
- **Course**: [Course Name]
- **Instructor**: [Instructor Name]
- **Semester**: [Semester/Year]

## License

This project is for educational purposes only.

## 📚 Documentation

- **[DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md)** - Step-by-step AWS deployment guide
- **[INFRASTRUCTURE_SETUP.md](./INFRASTRUCTURE_SETUP.md)** - Detailed infrastructure setup
- **[terraform/README.md](./terraform/README.md)** - Terraform configuration details
- **[docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)** - System architecture
- **[docs/API.md](./docs/API.md)** - API documentation
- **[docs/MONITORING.md](./docs/MONITORING.md)** - Monitoring design
- **[docs/BI_ANALYTICS.md](./docs/BI_ANALYTICS.md)** - Analytics design

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Next.js Documentation](https://nextjs.org/docs)
- [Clerk Documentation](https://clerk.com/docs)
- [WebSocket/STOMP Guide](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [Terraform Documentation](https://www.terraform.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Apache Superset](https://superset.apache.org/)
# Testing Jenkins CI/CD
# Testing Jenkins permissions
# Testing Git ownership fix
