# BuzzLink Frontend

Next.js frontend for BuzzLink enterprise chat application with Clerk authentication.

## Tech Stack

- **Next.js 14** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **Clerk** - Authentication (SSO + 2FA)
- **STOMP/WebSocket** - Real-time messaging
- **Axios** - HTTP client

## Setup

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment Variables

Copy `.env.local.example` to `.env.local` and fill in your Clerk credentials:

```bash
cp .env.local.example .env.local
```

Get your Clerk keys from [https://dashboard.clerk.com](https://dashboard.clerk.com)

### 3. Start Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:3000`

## Features Implemented

### Authentication
- Clerk-based sign up/sign in
- User profile management
- Session management
- Auto-sync with backend on login

### Real-Time Chat
- WebSocket connection with STOMP protocol
- Live message updates
- Channel-based conversations
- Message history loading

### User Experience
- Typing indicators
- Online presence tracking
- Emoji reactions (👍)
- File link messages
- Smooth scrolling

### Admin Features
- Admin badge display
- Message deletion (admin only)
- User management

## Project Structure

```
src/
├── app/                    # Next.js App Router pages
│   ├── chat/              # Main chat interface
│   ├── profile/           # User profile page
│   ├── sign-in/           # Clerk sign-in
│   └── sign-up/           # Clerk sign-up
├── components/            # React components
│   ├── Header.tsx
│   ├── ChannelSidebar.tsx
│   ├── ChatPanel.tsx
│   ├── MessageList.tsx
│   ├── MessageInput.tsx
│   ├── TypingIndicator.tsx
│   └── PresenceIndicator.tsx
├── hooks/                 # Custom React hooks
│   └── useWebSocket.ts
├── lib/                   # Utilities
│   ├── api.ts            # REST API client
│   └── websocket.ts      # WebSocket client
└── types/                 # TypeScript types
    └── index.ts
```

## Building for Production

```bash
npm run build
npm start
```

## Environment Variables

Required:
- `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY` - Clerk publishable key
- `CLERK_SECRET_KEY` - Clerk secret key

Optional:
- `NEXT_PUBLIC_API_URL` - Backend API URL (default: http://localhost:8080)
- `NEXT_PUBLIC_WS_URL` - WebSocket URL (default: http://localhost:8080/ws)

## Making a User Admin

For demo purposes, you can make a user admin by calling the backend API directly:

```bash
curl -X POST http://localhost:8080/api/users/make-admin \
  -H "Content-Type: application/json" \
  -d '{"clerkId": "user_xxxxx", "isAdmin": true}'
```

Replace `user_xxxxx` with the actual Clerk user ID.
