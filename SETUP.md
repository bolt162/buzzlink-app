# BuzzLink - Complete Setup Guide

This guide will walk you through setting up BuzzLink from scratch on your local machine.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Clerk Authentication Setup](#clerk-authentication-setup)
4. [Database Setup](#database-setup)
5. [Backend Setup](#backend-setup)
6. [Frontend Setup](#frontend-setup)
7. [Running the Application](#running-the-application)
8. [Creating Admin Users](#creating-admin-users)
9. [Troubleshooting](#troubleshooting)
10. [Verification Checklist](#verification-checklist)

---

## Prerequisites

Before you begin, ensure you have the following installed on your system:

### Required Software

1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   # Check if Java is installed
   java -version

   # Should output something like: java version "17.0.x"
   ```

   **Installation:**
   - **macOS**: `brew install openjdk@17`
   - **Windows**: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
   - **Linux**: `sudo apt install openjdk-17-jdk`

2. **Node.js 18 or higher (includes npm)**
   ```bash
   # Check if Node.js is installed
   node -v

   # Should output something like: v18.x.x or v20.x.x

   # Check npm
   npm -v
   ```

   **Installation:**
   - Download from [nodejs.org](https://nodejs.org/)
   - Or use nvm: `nvm install 18`

3. **Gradle (Optional - project includes wrapper)**
   ```bash
   # Check if Gradle is installed
   gradle -v
   ```

   **Note**: If Gradle is not installed, you can use the Gradle wrapper (`gradlew`) included in the project.

### Optional Software

4. **PostgreSQL** (Optional - H2 can be used for development)
   ```bash
   # Check if PostgreSQL is installed
   psql --version
   ```

   **Installation:**
   - **macOS**: `brew install postgresql`
   - **Windows**: Download from [postgresql.org](https://www.postgresql.org/download/)
   - **Linux**: `sudo apt install postgresql postgresql-contrib`
   - **Docker** (Recommended):
     ```bash
     docker run --name buzzlink-postgres \
       -e POSTGRES_PASSWORD=postgres \
       -e POSTGRES_DB=buzzlink \
       -p 5432:5432 \
       -d postgres:15
     ```

5. **Git**
   ```bash
   # Check if Git is installed
   git --version
   ```

---

## Initial Setup

### Step 1: Clone or Navigate to the Project

If you haven't already:

```bash
# Clone the repository (if using Git)
git clone <your-repo-url>
cd buzzlink

# Or navigate to the existing project directory
cd /path/to/buzzlink
```

### Step 2: Verify Project Structure

Ensure your project has the following structure:

```bash
ls -la
```

You should see:
```
buzzlink/
├── backend/
├── frontend/
├── docs/
├── Jenkinsfile
├── README.md
├── SETUP.md (this file)
└── .gitignore
```

---

## Clerk Authentication Setup

Clerk provides authentication for BuzzLink. You need to create a free account and get API keys.

### Step 1: Create Clerk Account

1. Go to [https://clerk.com](https://clerk.com)
2. Click "Start Building for Free"
3. Sign up with your email or GitHub account
4. Verify your email if required

### Step 2: Create Application

1. Once logged in, click "Create Application"
2. **Application Name**: `BuzzLink` (or any name you prefer)
3. **Authentication Options**: Enable:
   - ✅ Email
   - ✅ Password
   - ✅ (Optional) Google, GitHub, or other SSO providers
4. Click "Create Application"

### Step 3: Get API Keys

1. In the Clerk dashboard, go to **API Keys** (left sidebar)
2. Copy the following keys:
   - **Publishable Key** (starts with `pk_test_...`)
   - **Secret Key** (starts with `sk_test_...`)

**Important**: Keep these keys secure! Never commit them to Git.

### Step 4: Configure Clerk URLs

1. In Clerk dashboard, go to **Paths** (under User & Authentication)
2. Verify the default paths:
   - Sign-in: `/sign-in`
   - Sign-up: `/sign-up`
   - After sign-in: `/chat` (you may need to set this)
   - After sign-up: `/chat` (you may need to set this)

---

## Database Setup

You have two options: **H2 (Easy)** or **PostgreSQL (Production-like)**

### Option A: H2 Database (Recommended for Demo)

**No setup required!** H2 is an in-memory database that runs automatically.

- ✅ Zero configuration
- ✅ Perfect for demos and development
- ✅ Data is reset when you restart the backend
- ❌ Not suitable for production

**Skip to [Backend Setup](#backend-setup)**

### Option B: PostgreSQL (Production-like)

#### Using Docker (Easiest)

```bash
# Start PostgreSQL container
docker run --name buzzlink-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=buzzlink \
  -e POSTGRES_USER=postgres \
  -p 5432:5432 \
  -d postgres:15

# Verify it's running
docker ps | grep buzzlink-postgres

# (Optional) Connect to verify
docker exec -it buzzlink-postgres psql -U postgres -d buzzlink
# Type \q to exit
```

#### Using Local PostgreSQL Installation

```bash
# Start PostgreSQL (if not running)
# macOS:
brew services start postgresql

# Linux:
sudo systemctl start postgresql

# Create database
psql -U postgres
```

In the PostgreSQL prompt:
```sql
CREATE DATABASE buzzlink;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE buzzlink TO postgres;
\q
```

**Note**: If you use different credentials, update `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/buzzlink
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

---

## Backend Setup

### Step 1: Navigate to Backend Directory

```bash
cd backend
```

### Step 2: Generate Gradle Wrapper (if needed)

If `gradlew` doesn't exist or you get permission errors:

```bash
# On macOS/Linux:
chmod +x gradlew

# If gradlew doesn't exist and you have Gradle installed:
gradle wrapper --gradle-version 8.5
```

### Step 3: Build the Backend

```bash
# Clean and build the project
./gradlew clean build

# This will:
# - Download dependencies
# - Compile Java code
# - Run tests (if any)
# - Create JAR file
```

**Expected Output:**
```
BUILD SUCCESSFUL in 30s
```

**If build fails:**
- Check Java version: `java -version` (must be 17+)
- Check internet connection (Gradle needs to download dependencies)
- See [Troubleshooting](#troubleshooting) section

### Step 4: Configure Database Profile

Choose which database to use:

**For H2 (Easy Demo):**
```bash
# No changes needed! H2 is the default dev profile
```

**For PostgreSQL:**

Edit `backend/src/main/resources/application.properties` and verify:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/buzzlink
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## Frontend Setup

### Step 1: Navigate to Frontend Directory

```bash
cd ../frontend
# Or from project root:
cd frontend
```

### Step 2: Install Dependencies

```bash
# Install all npm packages (this may take 2-5 minutes)
npm install
```

**Expected Output:**
```
added 300+ packages in 2m
```

**If npm install fails:**
- Check Node version: `node -v` (must be 18+)
- Try clearing cache: `npm cache clean --force`
- Delete `node_modules` and try again: `rm -rf node_modules && npm install`

### Step 3: Create Environment File

```bash
# Copy the example environment file
cp .env.local.example .env.local
```

### Step 4: Add Clerk Keys to Environment File

Open `.env.local` in your text editor:

```bash
# Using nano:
nano .env.local

# Or using VS Code:
code .env.local

# Or using any text editor
```

Replace the placeholder values with your actual Clerk keys:

```bash
# Clerk Authentication
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_YOUR_ACTUAL_KEY_HERE
CLERK_SECRET_KEY=sk_test_YOUR_ACTUAL_KEY_HERE

# Clerk URLs (for Next.js App Router)
NEXT_PUBLIC_CLERK_SIGN_IN_URL=/sign-in
NEXT_PUBLIC_CLERK_SIGN_UP_URL=/sign-up
NEXT_PUBLIC_CLERK_AFTER_SIGN_IN_URL=/chat
NEXT_PUBLIC_CLERK_AFTER_SIGN_UP_URL=/chat

# Backend API
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
```

**Important**:
- Replace `pk_test_YOUR_ACTUAL_KEY_HERE` with your actual Clerk publishable key
- Replace `sk_test_YOUR_ACTUAL_KEY_HERE` with your actual Clerk secret key
- Save the file

### Step 5: Verify Frontend Build (Optional)

```bash
# Test the frontend build (optional, takes 1-2 minutes)
npm run build
```

This ensures everything is configured correctly before running.

---

## Running the Application

Now let's start both the backend and frontend!

### Terminal 1: Start Backend

```bash
# Navigate to backend directory
cd backend

# For H2 Database (Development):
./gradlew bootRun --args='--spring.profiles.active=dev'

# OR for PostgreSQL (Production-like):
./gradlew bootRun
```

**Wait for this message:**
```
✓ BuzzLink backend started successfully!
✓ Default channels initialized: general, random, engineering
✓ H2 Console (dev mode): http://localhost:8080/h2-console
✓ Prometheus metrics: http://localhost:8080/actuator/prometheus
```

**Backend is ready when you see:**
```
Started BuzzLinkApplication in X.XXX seconds
```

**Keep this terminal running!**

### Terminal 2: Start Frontend

Open a **new terminal window/tab**:

```bash
# Navigate to frontend directory
cd frontend

# Start the development server
npm run dev
```

**Wait for this message:**
```
✓ Ready in 2.5s
Local:        http://localhost:3000
```

**Keep this terminal running!**

### Step 3: Open Application in Browser

1. Open your web browser
2. Go to: **http://localhost:3000**
3. You should see the BuzzLink landing page

**Expected Landing Page:**
- BuzzLink title and logo
- "Sign In" and "Sign Up" buttons
- Features list
- Purple/blue gradient background

---

## Creating Admin Users

Admin users can delete messages. Here's how to make a user an admin:

### Step 1: Create a Regular User

1. Go to http://localhost:3000
2. Click "Sign Up"
3. Enter your email and password
4. Complete sign-up through Clerk
5. You'll be redirected to the chat page

### Step 2: Get Your Clerk User ID

Once logged in:

1. Click on your profile picture or name in the header
2. Click "Profile"
3. Look for "User ID" in the account info section
4. Copy the User ID (format: `user_2XXXXXXXXXXXXX`)

### Step 3: Make User Admin via API

Open a **new terminal window** and run:

```bash
curl -X POST http://localhost:8080/api/users/make-admin \
  -H "Content-Type: application/json" \
  -d '{"clerkId": "user_YOUR_CLERK_ID_HERE", "isAdmin": true}'
```

**Replace** `user_YOUR_CLERK_ID_HERE` with your actual Clerk User ID.

**Expected Response:**
```
HTTP 200 OK
```

### Step 4: Verify Admin Status

1. Refresh the chat page (http://localhost:3000/chat)
2. You should see a yellow "ADMIN" badge next to your name in the header
3. When you hover over messages, you should see a delete button (🗑️)

**Alternative Method (Database):**

If using H2:
1. Go to http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:buzzlink`
3. User: `sa`
4. Password: (leave empty)
5. Run SQL:
   ```sql
   UPDATE users SET is_admin = true WHERE clerk_id = 'user_YOUR_ID';
   ```

---

## Troubleshooting

### Backend Issues

#### Port 8080 Already in Use

**Error:** `Port 8080 is already in use`

**Solution:**
```bash
# Find what's using port 8080
# macOS/Linux:
lsof -i :8080

# Windows:
netstat -ano | findstr :8080

# Kill the process or change backend port in application.properties:
server.port=8081
```

#### Java Version Mismatch

**Error:** `has been compiled by a more recent version of the Java Runtime`

**Solution:**
```bash
# Check your Java version
java -version

# Should be 17 or higher
# If not, install Java 17+ and set JAVA_HOME
```

#### Database Connection Failed

**Error:** `Could not connect to PostgreSQL`

**Solution:**
```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Or check PostgreSQL service
# macOS:
brew services list

# Linux:
sudo systemctl status postgresql

# Restart PostgreSQL
docker restart buzzlink-postgres
```

#### Gradle Build Failed

**Error:** `Could not download dependencies`

**Solution:**
```bash
# Check internet connection
# Clear Gradle cache
rm -rf ~/.gradle/caches

# Try building again
./gradlew clean build --refresh-dependencies
```

### Frontend Issues

#### npm install Failed

**Error:** `ERESOLVE unable to resolve dependency tree`

**Solution:**
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall
npm install

# If still failing, try legacy peer deps
npm install --legacy-peer-deps
```

#### Clerk Keys Not Working

**Error:** `Clerk: Missing publishable key`

**Solution:**
1. Verify `.env.local` exists in `frontend/` directory
2. Check keys don't have extra spaces or quotes
3. Restart the frontend dev server (`npm run dev`)
4. Hard refresh browser (Cmd+Shift+R or Ctrl+Shift+R)

#### Port 3000 Already in Use

**Error:** `Port 3000 is already in use`

**Solution:**
```bash
# Kill process on port 3000
# macOS/Linux:
lsof -i :3000
kill -9 <PID>

# Or run on different port
npm run dev -- -p 3001
```

#### WebSocket Not Connecting

**Error:** In browser console: `WebSocket connection failed`

**Solution:**
1. Verify backend is running on port 8080
2. Check backend logs for errors
3. Clear browser cache and cookies
4. Try incognito/private browsing mode

### General Issues

#### Nothing Appears After Sign In

**Solution:**
1. Open browser DevTools (F12)
2. Check Console tab for errors
3. Check Network tab for failed requests
4. Verify backend is running and healthy:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

#### Messages Not Appearing

**Solution:**
1. Check browser console for WebSocket errors
2. Verify backend WebSocket is working:
   ```bash
   # Check backend logs for WebSocket connections
   ```
3. Refresh the page
4. Try in a different browser

---

## Verification Checklist

Use this checklist to verify everything is working:

### Backend Health Check

- [ ] Backend started without errors
- [ ] Health endpoint responds: `curl http://localhost:8080/actuator/health`
- [ ] Channels endpoint responds: `curl http://localhost:8080/api/channels`
- [ ] See 3 channels: general, random, engineering

### Frontend Health Check

- [ ] Frontend started without errors
- [ ] Landing page loads at http://localhost:3000
- [ ] No errors in browser console (F12)

### Authentication Check

- [ ] Can click "Sign Up"
- [ ] Clerk sign-up form appears
- [ ] Can create account
- [ ] Redirected to chat page after sign-up
- [ ] Can sign out
- [ ] Can sign back in

### Chat Features Check

- [ ] Can see channel list (general, random, engineering)
- [ ] Can click a channel
- [ ] Can type a message
- [ ] Message appears after clicking Send
- [ ] Online user count shows "1 user online"

### Real-Time Features Check (Need 2 browsers)

- [ ] Open second browser (or incognito window)
- [ ] Sign in with different account
- [ ] Both users join #general
- [ ] Online count shows "2 users online"
- [ ] Send message in browser 1
- [ ] Message appears in browser 2 instantly
- [ ] Start typing in browser 1
- [ ] "User is typing..." appears in browser 2

### Admin Features Check

- [ ] Make user admin via curl command
- [ ] Refresh page
- [ ] See "ADMIN" badge in header
- [ ] Hover over message
- [ ] See delete button (🗑️)
- [ ] Click delete
- [ ] Confirm deletion
- [ ] Message disappears

### Optional Features Check

- [ ] Click 👍 on a message
- [ ] Counter increments
- [ ] Click 👍 again
- [ ] Counter decrements
- [ ] Switch to "File Link" mode
- [ ] Paste URL and send
- [ ] Message shows as link with 📎 icon
- [ ] Click on file link
- [ ] Opens in new tab

---

## Quick Start Summary

For future reference, here's the quick start:

```bash
# Terminal 1 - Backend (H2)
cd backend
./run.sh
# Or manually with correct Java version:
# export JAVA_HOME=$(/usr/libexec/java_home -v 17) && ./gradlew bootRun --args='--spring.profiles.active=dev'

# Terminal 2 - Frontend
cd frontend
npm run dev

# Browser
# Open http://localhost:3000
```

**First-time setup only:**
1. Create Clerk account and get API keys
2. Add keys to `frontend/.env.local`
3. Run `npm install` in frontend directory
4. Run `./gradlew build` in backend directory

---

## Next Steps

Now that everything is running:

1. **Test the Demo**: Go through all features to ensure they work
2. **Create Test Accounts**: Create 2-3 test users for your demo
3. **Make One Admin**: Use the curl command to create an admin user
4. **Prepare Presentation**: Review `docs/PRESENTATION_GUIDE.md`
5. **Read Architecture**: Understand the system with `docs/ARCHITECTURE.md`

---

## Getting Help

If you encounter issues not covered here:

1. **Check Logs**: Look at backend and frontend terminal output
2. **Browser Console**: Open DevTools (F12) and check for errors
3. **Documentation**: Read `README.md` and files in `docs/` directory
4. **Network Tab**: Check if API calls are succeeding

---

## Stopping the Application

When you're done:

```bash
# In each terminal, press:
Ctrl + C

# To stop PostgreSQL Docker container (if using):
docker stop buzzlink-postgres

# To remove PostgreSQL container completely:
docker rm buzzlink-postgres
```

---

**You're all set! 🚀**

Your BuzzLink application should now be running. Head to http://localhost:3000 to start chatting!
