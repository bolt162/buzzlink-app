#!/bin/bash

# Stop all BuzzLink services

echo "Stopping BuzzLink services..."
docker-compose down

echo ""
echo "Services stopped successfully!"
echo ""
echo "To remove volumes (including database data), run:"
echo "  docker-compose down -v"
