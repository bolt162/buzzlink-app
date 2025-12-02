# BuzzLink Architecture

## System Overview

BuzzLink is an enterprise-grade chat application designed to demonstrate modern software architecture patterns and technologies commonly used in production systems.

## High-Level Architecture

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│              │         │              │         │              │
│   Next.js    │◄───────►│  Spring Boot │◄───────►│  PostgreSQL  │
│   Frontend   │  REST   │   Backend    │   JPA   │   Database   │
│              │  + WS   │              │         │              │
└──────────────┘         └──────────────┘         └──────────────┘
       │                        │
       │                        │
       ▼                        ▼
┌──────────────┐         ┌──────────────┐
│              │         │              │
│    Clerk     │         │    Kafka     │
│  (Auth/SSO)  │         │ (Events/Msg) │
│              │         │              │
└──────────────┘         └──────────────┘

                        ┌──────────────┐
                        │              │
                        │  Prometheus  │
                        │  (Metrics)   │
                        │              │
                        └──────────────┘
                                │
                                ▼
                        ┌──────────────┐
                        │              │
                        │   Superset   │
                        │ (Analytics)  │
                        │              │
                        └──────────────┘
```

## Component Breakdown

### Frontend Layer

**Technology:** Next.js 14 with App Router, TypeScript, Tailwind CSS

**Responsibilities:**
- User interface rendering
- Client-side routing
- WebSocket connection management
- Authentication state management (via Clerk)
- Real-time UI updates

**Key Features:**
- Server-side rendering (SSR) for initial page loads
- Client-side navigation for app-like experience
- Optimistic UI updates for better UX
- Responsive design for mobile/desktop

### Authentication & Authorization

**Technology:** Clerk

**Features:**
- Social sign-on (Google, GitHub, etc.)
- Email/password authentication
- Two-factor authentication (2FA)
- Session management
- User metadata storage

**Flow:**
1. User signs in via Clerk
2. Frontend receives JWT token
3. Token sent to backend with each request
4. Backend validates token (in production - stub in demo)
5. User synced to database via Clerk ID

### Backend Layer

**Technology:** Spring Boot 3.2, Java 17

**Responsibilities:**
- Business logic
- Data persistence
- WebSocket connection management
- Real-time message broadcasting
- API endpoint provisioning

**Architecture Pattern:** Layered Architecture
- Controllers (REST + WebSocket)
- Services (Business Logic)
- Repositories (Data Access)
- Entities (Domain Models)

### Data Layer

**Technology:** PostgreSQL (Production), H2 (Development)

**Schema:**
- `users` - User profiles linked to Clerk IDs
- `channels` - Chat channels
- `messages` - Chat messages with channel and user references
- `reactions` - Emoji reactions to messages

**Indexing Strategy:**
- Index on `channel_id, created_at` for message queries
- Unique constraint on `clerk_id` for users
- Composite index on `message_id, user_id` for reactions

### Real-Time Communication

**Technology:** WebSocket with STOMP protocol

**Communication Patterns:**
- **Pub/Sub:** Channels use topic-based messaging
  - `/topic/channel.{id}` - Message broadcasts
  - `/topic/channel.{id}.typing` - Typing indicators
  - `/topic/channel.{id}.presence` - Presence updates

**Connection Lifecycle:**
1. Client connects to `/ws` endpoint
2. Client subscribes to channel topics
3. Client sends join event
4. Server tracks presence
5. Real-time bidirectional communication
6. Client sends leave event on disconnect

### Event Streaming (Design Only)

**Technology:** Apache Kafka

**Purpose:** Decouple message processing from real-time delivery

**Topics:**
- `buzzlink.messages.new` - New message events
- `buzzlink.messages.deleted` - Deletion events
- `buzzlink.mentions` - User mention events
- `buzzlink.reactions` - Reaction events

**Consumers:**
- Email notification service
- Push notification service
- Search indexing service
- Analytics pipeline

**Producer Integration:**
Located in `NotificationService.java` (currently stubbed)

### Monitoring & Metrics (Design Only)

**Technology:** Prometheus + Grafana

**Metrics Collected:**
- HTTP request rates and latencies
- WebSocket connection counts
- Message throughput
- Database query performance
- JVM metrics (heap, GC, threads)

**Endpoints:**
- `/actuator/prometheus` - Prometheus scrape endpoint
- `/actuator/health` - Health check
- `/actuator/metrics` - Metrics browser

**Dashboard Panels:**
- System health overview
- Request/response times
- Active WebSocket connections
- Message delivery rates
- Error rates

### Business Intelligence (Design Only)

**Technology:** Apache Superset

**Analytics Use Cases:**
- Daily active users (DAU)
- Messages per channel
- Peak usage hours
- User engagement metrics
- Channel popularity

See `BI_ANALYTICS.md` for detailed queries and dashboard designs.

## Security Considerations

### Authentication
- Clerk handles authentication securely
- JWT tokens for API authorization (production)
- HTTPS required in production

### Data Protection
- SQL injection prevention via JPA/Hibernate
- XSS protection via React's built-in escaping
- CSRF tokens for state-changing operations (production)

### WebSocket Security
- Origin validation
- Session-based authentication
- Rate limiting (production)

## Scalability Considerations

### Horizontal Scaling
- **Frontend:** Deploy multiple Next.js instances behind load balancer
- **Backend:** Stateless Spring Boot instances with sticky sessions disabled
- **Database:** Read replicas for query scaling
- **WebSocket:** Shared message broker (Redis Pub/Sub or Kafka)

### Vertical Scaling
- Increase JVM heap size
- Database connection pool tuning
- WebSocket connection limits

### Caching Strategy
- **Redis** for session data
- **CDN** for static assets
- **Query caching** for channel lists

## Deployment Architecture (Production)

```
                    ┌──────────────┐
                    │              │
                    │  CloudFlare  │
                    │     CDN      │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │              │
                    │ Load Balancer│
                    │              │
                    └──────┬───────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐       ┌────▼────┐      ┌────▼────┐
    │         │       │         │      │         │
    │Next.js  │       │Next.js  │      │Next.js  │
    │Instance │       │Instance │      │Instance │
    └─────────┘       └─────────┘      └─────────┘
         │                 │                 │
         └─────────────────┼─────────────────┘
                           │
                    ┌──────▼───────┐
                    │              │
                    │   Backend    │
                    │ Load Balancer│
                    └──────┬───────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐       ┌────▼────┐      ┌────▼────┐
    │ Spring  │       │ Spring  │      │ Spring  │
    │  Boot   │       │  Boot   │      │  Boot   │
    └────┬────┘       └────┬────┘      └────┬────┘
         │                 │                 │
         └─────────────────┼─────────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐       ┌────▼────┐      ┌────▼────┐
    │Postgres │       │  Redis  │      │  Kafka  │
    │ Primary │       │  Cache  │      │ Cluster │
    └────┬────┘       └─────────┘      └─────────┘
         │
    ┌────▼────┐
    │Postgres │
    │ Replica │
    └─────────┘
```

## Technology Choices - Rationale

### Why Spring Boot?
- Enterprise-standard Java framework
- Excellent WebSocket support
- Rich ecosystem of libraries
- Easy integration with monitoring tools

### Why Next.js?
- Modern React framework with SSR
- Great developer experience
- Built-in routing and API routes
- Optimized production builds

### Why Clerk?
- Turnkey authentication solution
- SSO and 2FA out of the box
- Developer-friendly API
- Reduces security risks

### Why PostgreSQL?
- ACID compliance
- Rich querying capabilities
- Excellent performance
- Industry-standard for relational data

### Why Kafka?
- High-throughput event streaming
- Decouples producers from consumers
- Persistent event log
- Standard for enterprise messaging

## Future Enhancements

1. **Direct Messaging:** 1-on-1 conversations
2. **Thread Replies:** Message threading like Slack
3. **File Uploads:** Actual file storage (S3)
4. **Search:** Full-text search with Elasticsearch
5. **Video/Voice:** WebRTC integration
6. **Mobile Apps:** React Native clients
