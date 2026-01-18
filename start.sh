#!/bin/bash
# Start the entire cinema-booking application stack

set -e

echo "Starting Cinema Booking Application Stack..."
echo "============================================"
echo ""
echo "Starting services:"
echo "  - PostgreSQL (port 5432)"
echo "  - pgAdmin (port 5050)"
echo "  - RabbitMQ (ports 5672, 15672, 61613)"
echo "  - Cinema App (port 8080)"
echo ""
echo "The application will be available at: http://localhost:8080"
echo "Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo ""

# Start in background
docker compose up -d

# Show status
docker compose ps

# Tail app logs
echo ""
echo "Following app logs (Ctrl+C to stop)..."
docker compose logs -f cinema-app