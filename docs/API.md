# BuzzLink API Documentation

> **Interactive Documentation**: See [OpenAPI Specification](openapi.yaml) or view the [Swagger UI Dashboard](swagger-ui.html) for interactive API testing.

## Quick Reference

### 1. Direct Messages (DMs)
- `GET /api/direct-messages/{recipientClerkId}` - Get DM conversation
- `POST /api/direct-messages` - Send DM
- **WebSocket**: `/topic/dm.{clerkId}` - Subscribe to DMs
- **WebSocket**: `/app/dm.send` - Send DM via WebSocket

### 2. Workspaces
- `GET /api/workspaces` - List user's workspaces
- `POST /api/workspaces` - Create workspace
- `GET /api/workspaces/{id}` - Get workspace details
- `PUT /api/workspaces/{id}` - Update workspace
- `DELETE /api/workspaces/{id}` - Delete workspace
- `GET /api/workspaces/{id}/channels` - Get workspace channels
- `POST /api/workspaces/{id}/channels` - Create channel in workspace
- `GET /api/workspaces/{id}/members` - List workspace members
- `POST /api/workspaces/{id}/members/{userId}/role` - Update member role
- `DELETE /api/workspaces/{id}/members/{userId}` - Remove member

### 3. Workspace Invitations
- `POST /api/workspaces/{id}/invitations` - Send invitation
- `GET /api/invitations` - List user's invitations
- `POST /api/invitations/{token}/accept` - Accept invitation
- `POST /api/invitations/{token}/decline` - Decline invitation

### 4. Notifications
- `GET /api/notifications` - List user's notifications
- `PUT /api/notifications/{id}/read` - Mark notification as read
- `PUT /api/notifications/read-all` - Mark all as read
- `DELETE /api/notifications/{id}` - Delete notification
- **WebSocket**: `/topic/notifications.{clerkId}` - Subscribe to notifications

### 5. Message Threading
- `GET /api/messages/{messageId}/replies` - Get thread replies
- `POST /api/messages/{messageId}/replies` - Reply to thread
- Message object includes `parentMessageId` and `replyCount`

### 6. User Management
- `GET /api/users` - Search/list users
- `GET /api/users/{clerkId}` - Get user by Clerk ID

### 7. Analytics API
- `GET /api/analytics/workspace/{workspaceId}/overview` - Workspace stats
- `GET /api/analytics/workspace/{workspaceId}/activity` - Activity over time
- `GET /api/analytics/workspace/{workspaceId}/top-users` - Most active users
- `GET /api/analytics/workspace/{workspaceId}/top-channels` - Most active channels

## Authentication

All API endpoints require authentication using Clerk JWT tokens. Include the token in the Authorization header:

```
Authorization: Bearer <your-clerk-jwt-token>
```

## Resources

- **OpenAPI Spec**: [openapi.yaml](openapi.yaml)
- **Interactive Docs**: [swagger-ui.html](swagger-ui.html)
- **Deployment Guide**: [DEPLOYMENT.md](DEPLOYMENT.md)