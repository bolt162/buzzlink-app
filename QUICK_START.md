# BuzzLink - Quick Start Guide 🚀

Everything is set up and ready to run!

## Running BuzzLink (2 Terminals)

### Terminal 1: Start Backend

```bash
cd /Users/kartikeysharma/buzzlink/backend
./run.sh
```

**Wait for:**
```
✓ BuzzLink backend started successfully!
✓ Default channels initialized: general, random, engineering
```

**Backend URL:** http://localhost:8080

---

### Terminal 2: Start Frontend

```bash
cd /Users/kartikeysharma/buzzlink/frontend
npm run dev
```

**Wait for:**
```
✓ Ready in 2.5s
Local:        http://localhost:3000
```

**Frontend URL:** http://localhost:3000

---

## Open in Browser

Go to: **http://localhost:3000**

You should see the BuzzLink landing page with:
- Sign In / Sign Up buttons
- Features list
- Purple/blue gradient background

## First-Time Setup (Already Done ✅)

- ✅ Gradle installed and wrapper created
- ✅ Backend built successfully
- ✅ Frontend dependencies installed (173 packages)
- ✅ Clerk API keys configured
- ✅ Environment variables set up

## Testing Your Setup

1. **Backend Health Check:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Should return: `{"status":"UP"}`

2. **Channels Endpoint:**
   ```bash
   curl http://localhost:8080/api/channels
   ```
   Should return 3 channels: general, random, engineering

3. **Frontend Loads:**
   Open http://localhost:3000 in browser

## Quick Demo Steps

1. **Sign Up**
   - Click "Sign Up"
   - Enter email and password
   - Verify email if prompted
   - You'll be redirected to chat

2. **Send a Message**
   - You'll see #general, #random, #engineering channels
   - Click on a channel
   - Type a message and click Send
   - Message appears instantly

3. **Test Real-Time (Optional)**
   - Open a second browser (or incognito)
   - Sign in with different account
   - Both users join #general
   - Send message from one browser
   - See it appear in the other

4. **Make Yourself Admin**
   - After signing in, go to Profile (top right)
   - Copy your User ID
   - In a new terminal:
   ```bash
   curl -X POST http://localhost:8080/api/users/make-admin \
     -H "Content-Type: application/json" \
     -d '{"clerkId": "YOUR_USER_ID_HERE", "isAdmin": true}'
   ```
   - Refresh the page
   - You'll see "ADMIN" badge
   - Hover over messages to see delete button

## Stopping the Application

In each terminal, press: **Ctrl + C**

## Troubleshooting

**Backend won't start?**
- Check Java version: `java -version` (should be 17)
- Port 8080 in use? Kill the process or change port

**Frontend won't start?**
- Node.js installed? `node -v` (should be 18+)
- Try: `rm -rf .next && npm run dev`

**Can't sign in?**
- Check `.env.local` has Clerk keys
- Verify Clerk dashboard is active

**WebSocket not connecting?**
- Backend must be running first
- Check browser console for errors

## Complete File Structure

Your project now has:

```
buzzlink/
├── backend/
│   ├── gradlew ✅
│   ├── run.sh ✅
│   ├── build.gradle ✅
│   └── src/ (all Java files)
├── frontend/
│   ├── node_modules/ ✅ (173 packages)
│   ├── .env.local ✅ (with Clerk keys)
│   └── src/ (all React files)
├── docs/ (5 documentation files)
├── SETUP.md
├── GRADLE_SETUP_COMPLETE.md
├── FRONTEND_SETUP_COMPLETE.md
└── QUICK_START.md (this file)
```

## What's Working

✅ Spring Boot backend with H2 database
✅ REST API endpoints
✅ WebSocket real-time messaging
✅ Next.js frontend
✅ Clerk authentication
✅ 3 default channels
✅ Message history
✅ Typing indicators
✅ Online presence
✅ Emoji reactions
✅ File link messages
✅ Admin message deletion

## What's Designed (Not Fully Running)

📋 Kafka event streaming
📋 Prometheus monitoring
📋 Apache Superset analytics
📋 Jenkins CI/CD pipeline

## Need Help?

- **Setup Guide:** `SETUP.md`
- **Backend Details:** `GRADLE_SETUP_COMPLETE.md`
- **Frontend Details:** `FRONTEND_SETUP_COMPLETE.md`
- **Presentation Guide:** `docs/PRESENTATION_GUIDE.md`
- **Architecture:** `docs/ARCHITECTURE.md`
- **API Docs:** `docs/API.md`

---

**You're all set! Start both terminals and open http://localhost:3000** 🎉
