# Business Intelligence & Analytics with Apache Superset

## Overview

This document describes how Apache Superset would be integrated with BuzzLink to provide business intelligence and analytics dashboards for understanding user engagement, system performance, and content patterns.

## Data Sources

Superset would connect to the PostgreSQL database with read-only access to the following tables:
- `users`
- `channels`
- `messages`
- `reactions`

## SQL Views for Analytics

These views would be created in PostgreSQL to simplify dashboard creation:

### 1. Daily Active Users (DAU)

```sql
CREATE VIEW daily_active_users AS
SELECT
  DATE(m.created_at) as date,
  COUNT(DISTINCT m.sender_id) as active_users
FROM messages m
GROUP BY DATE(m.created_at)
ORDER BY date DESC;
```

**Dashboard Use:** Track user engagement trends over time

### 2. Messages Per Channel

```sql
CREATE VIEW messages_per_channel AS
SELECT
  c.name as channel_name,
  c.description,
  COUNT(m.id) as message_count,
  COUNT(DISTINCT m.sender_id) as unique_contributors,
  MAX(m.created_at) as last_message_at
FROM channels c
LEFT JOIN messages m ON c.id = m.channel_id
GROUP BY c.id, c.name, c.description
ORDER BY message_count DESC;
```

**Dashboard Use:** Identify most active channels and potential ghost channels

### 3. Peak Usage Hours

```sql
CREATE VIEW peak_usage_hours AS
SELECT
  EXTRACT(HOUR FROM created_at) as hour_of_day,
  COUNT(*) as message_count,
  COUNT(DISTINCT sender_id) as active_users
FROM messages
GROUP BY EXTRACT(HOUR FROM created_at)
ORDER BY hour_of_day;
```

**Dashboard Use:** Understand when users are most active for maintenance scheduling

### 4. Top Contributors

```sql
CREATE VIEW top_contributors AS
SELECT
  u.display_name,
  u.email,
  COUNT(m.id) as messages_sent,
  COUNT(DISTINCT m.channel_id) as channels_participated,
  MIN(m.created_at) as first_message_at,
  MAX(m.created_at) as last_message_at
FROM users u
LEFT JOIN messages m ON u.id = m.sender_id
GROUP BY u.id, u.display_name, u.email
ORDER BY messages_sent DESC;
```

**Dashboard Use:** Identify power users and inactive accounts

### 5. Engagement Metrics

```sql
CREATE VIEW engagement_metrics AS
SELECT
  DATE(m.created_at) as date,
  c.name as channel_name,
  COUNT(m.id) as messages,
  COUNT(DISTINCT m.sender_id) as unique_senders,
  COUNT(r.id) as total_reactions,
  ROUND(COUNT(r.id)::DECIMAL / NULLIF(COUNT(m.id), 0), 2) as reactions_per_message
FROM messages m
LEFT JOIN channels c ON m.channel_id = c.id
LEFT JOIN reactions r ON m.id = r.message_id
GROUP BY DATE(m.created_at), c.id, c.name
ORDER BY date DESC, messages DESC;
```

**Dashboard Use:** Track engagement quality, not just quantity

### 6. User Growth

```sql
CREATE VIEW user_growth AS
SELECT
  DATE(created_at) as signup_date,
  COUNT(*) as new_users,
  SUM(COUNT(*)) OVER (ORDER BY DATE(created_at)) as cumulative_users
FROM users
GROUP BY DATE(created_at)
ORDER BY signup_date;
```

**Dashboard Use:** Monitor user acquisition trends

### 7. Message Type Distribution

```sql
CREATE VIEW message_type_distribution AS
SELECT
  type,
  COUNT(*) as count,
  ROUND(COUNT(*)::DECIMAL / SUM(COUNT(*)) OVER () * 100, 2) as percentage
FROM messages
GROUP BY type;
```

**Dashboard Use:** Understand how users share content (text vs files)

### 8. Response Time Analysis

```sql
CREATE VIEW channel_response_times AS
WITH message_intervals AS (
  SELECT
    m1.channel_id,
    m1.created_at,
    m1.sender_id,
    EXTRACT(EPOCH FROM (
      m1.created_at - LAG(m1.created_at) OVER (
        PARTITION BY m1.channel_id
        ORDER BY m1.created_at
      )
    )) / 60 as minutes_since_last
  FROM messages m1
)
SELECT
  c.name as channel_name,
  AVG(minutes_since_last) as avg_response_minutes,
  PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY minutes_since_last) as median_response_minutes
FROM message_intervals mi
JOIN channels c ON mi.channel_id = c.id
WHERE minutes_since_last IS NOT NULL
  AND minutes_since_last < 1440 -- exclude gaps > 24 hours
GROUP BY c.id, c.name;
```

**Dashboard Use:** Measure channel activity and responsiveness

## Superset Dashboard Designs

### Dashboard 1: Executive Overview

**Purpose:** High-level metrics for leadership

**Charts:**
1. **KPI Cards:**
   - Total users
   - Daily active users (today)
   - Total messages (all time)
   - Messages today

2. **Line Chart:** Daily active users trend (30 days)

3. **Bar Chart:** Messages per channel (top 10)

4. **Pie Chart:** User activity distribution (active/inactive)

### Dashboard 2: User Engagement

**Purpose:** Deep dive into user behavior

**Charts:**
1. **Heat Map:** Message activity by hour and day of week

2. **Table:** Top 20 contributors with metrics

3. **Line Chart:** User growth over time

4. **Bar Chart:** Messages per user (distribution)

5. **Metric:** Average reactions per message

### Dashboard 3: Channel Analytics

**Purpose:** Channel performance and health

**Charts:**
1. **Table:** All channels with stats (messages, contributors, last activity)

2. **Bar Chart:** Engagement by channel (reactions per message)

3. **Line Chart:** Channel growth (messages over time per channel)

4. **Alert:** Inactive channels (no messages in 7 days)

### Dashboard 4: Content Analytics

**Purpose:** Understand what content is shared

**Charts:**
1. **Pie Chart:** Message type distribution (text vs file)

2. **Timeline:** Peak usage hours

3. **Bar Chart:** Most reacted messages (top 50)

4. **Trend:** Reaction engagement over time

### Dashboard 5: System Health

**Purpose:** Operational monitoring

**Charts:**
1. **Line Chart:** Messages per hour (real-time)

2. **KPI:** Average response time per channel

3. **Table:** Error log summary (would integrate with logging)

4. **Alert:** Unusual activity patterns

## Sample Superset Configuration

### Database Connection

```python
# Superset database connection string
SQLALCHEMY_DATABASE_URI = 'postgresql://superset_readonly:password@localhost:5432/buzzlink'
```

### Read-Only User Setup

```sql
-- Create read-only user for Superset
CREATE USER superset_readonly WITH PASSWORD 'secure_password';

-- Grant connect
GRANT CONNECT ON DATABASE buzzlink TO superset_readonly;

-- Grant usage on schema
GRANT USAGE ON SCHEMA public TO superset_readonly;

-- Grant select on tables
GRANT SELECT ON ALL TABLES IN SCHEMA public TO superset_readonly;

-- Grant select on future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT ON TABLES TO superset_readonly;

-- Grant select on views
GRANT SELECT ON ALL VIEWS IN SCHEMA public TO superset_readonly;
```

## Scheduled Exports

Superset dashboards can be configured to send automated reports:

1. **Daily Executive Summary:** Email to leadership at 8 AM
2. **Weekly Engagement Report:** Email to product team every Monday
3. **Monthly Growth Report:** Email to all stakeholders

## Alert Configuration

Set up alerts for:
- DAU drops below threshold (e.g., < 50% of 30-day average)
- No messages in a channel for 7 days
- Single user sending > 1000 messages/day (potential spam)
- System errors spike (> 100 errors/hour)

## Integration with BuzzLink

### Data Refresh Strategy

- **Real-time:** Not required for BI (analytics are typically T+1)
- **Batch refresh:** Every 15 minutes during business hours
- **Full refresh:** Nightly at 2 AM

### Performance Optimization

1. **Materialized Views:** For complex queries
2. **Indexes:** On frequently queried columns (created_at, channel_id, sender_id)
3. **Query Caching:** Enable Superset query result caching (1 hour)
4. **Connection Pooling:** Use read replicas to avoid impacting production

## Access Control

Superset roles mapped to BuzzLink permissions:
- **Admin:** Full access to all dashboards and data
- **Manager:** Access to engagement and channel analytics
- **Analyst:** Read-only access to specific dashboards
- **Executive:** Access to executive overview only

## Future Enhancements

1. **Sentiment Analysis:** Integrate ML to analyze message sentiment
2. **Predictive Analytics:** Forecast user growth and churn
3. **A/B Testing Dashboard:** Track feature experiments
4. **Real-time Dashboard:** Live message feed for monitoring
5. **Custom Metrics:** Allow teams to define their own KPIs
