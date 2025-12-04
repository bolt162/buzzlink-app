# BuzzLink Backend

Enterprise chat application backend built with Spring Boot.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** - Database access
- **Spring WebSocket** - Real-time messaging
- **Spring Actuator** - Metrics and monitoring
- **PostgreSQL** - Production database
- **H2** - Development database
- **Kafka** - Notification system (design/stub)

## Running the Backend

### Development Mode (H2 Database)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The backend will start on `http://localhost:8080` with:
- H2 in-memory database
- H2 Console at `http://localhost:8080/h2-console`
- Default channels: general, random, engineering

### Production Mode (PostgreSQL)

1. Start PostgreSQL:
```bash
docker run --name buzzlink-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=buzzlink -p 5432:5432 -d postgres:15
```

2. Run the application:
```bash
./gradlew bootRun
```

## API Endpoints

### Workspaces (NEW)
- `GET /api/workspaces?clerkId={clerkId}` - List user's workspaces
- `GET /api/workspaces/{slug}?clerkId={clerkId}` - Get workspace by slug
- `POST /api/workspaces` - Create workspace
- `POST /api/workspaces/{workspaceId}/members` - Add member to workspace

### Channels
- `GET /api/channels` - List all channels (optionally filter by workspaceId)
- `GET /api/channels?workspaceId={id}` - List channels in workspace
- `GET /api/channels/{id}` - Get channel details
- `POST /api/channels` - Create channel (requires workspaceId)

### Messages
- `GET /api/channels/{channelId}/messages` - Get recent messages
- `DELETE /api/messages/{messageId}` - Delete message (admin only)
- `POST /api/messages/{messageId}/reactions` - Toggle reaction

### Direct Messages (NEW)
- `GET /api/direct-messages/conversations?clerkId={clerkId}` - List all conversations
- `GET /api/direct-messages/conversation/{otherUserId}?clerkId={clerkId}&limit=50` - Get messages with user
- `POST /api/direct-messages` - Send direct message

### Users
- `POST /api/users/sync` - Sync user from Clerk
- `GET /api/users/me` - Get current user
- `PUT /api/users/me` - Update profile
- `POST /api/users/make-admin` - Make user admin (demo only)

## WebSocket Endpoints

Connect to: `ws://localhost:8080/ws` with SockJS

### Client Sends
- `/app/chat.sendMessage` - Send a channel message
- `/app/chat.typing` - Typing indicator
- `/app/chat.join` - Join a channel
- `/app/chat.leave` - Leave a channel
- `/app/dm.send` - Send direct message (NEW)

### Client Subscribes
- `/topic/channel.{channelId}` - Receive channel messages
- `/topic/channel.{channelId}.typing` - Typing indicators
- `/topic/channel.{channelId}.presence` - Presence updates
- `/user/queue/messages` - Receive direct messages (NEW)

## Monitoring

- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

## Building

```bash
./gradlew build
```

## Running Tests

```bash
./gradlew test
```
