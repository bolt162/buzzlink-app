-- BuzzLink Mock Data for Power BI Dashboard
-- This script generates realistic test data for analytics and reporting

-- Clear existing data (in reverse order of foreign key dependencies)
DELETE FROM reactions;
DELETE FROM notifications;
DELETE FROM messages;
DELETE FROM direct_messages;
DELETE FROM user_workspace_members;
DELETE FROM workspace_invitations;
DELETE FROM channels;
DELETE FROM workspaces;
DELETE FROM users;

-- Reset sequences
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE workspaces_id_seq RESTART WITH 1;
ALTER SEQUENCE channels_id_seq RESTART WITH 1;
ALTER SEQUENCE messages_id_seq RESTART WITH 1;
ALTER SEQUENCE direct_messages_id_seq RESTART WITH 1;
ALTER SEQUENCE notifications_id_seq RESTART WITH 1;
ALTER SEQUENCE reactions_id_seq RESTART WITH 1;
ALTER SEQUENCE user_workspace_members_id_seq RESTART WITH 1;

-- ================================================================================
-- USERS (20 mock users with realistic names)
-- ================================================================================
INSERT INTO users (clerk_id, display_name, avatar_url, is_admin, is_banned, email, created_at, updated_at) VALUES
('mock_user_1', 'Sarah Johnson', 'https://i.pravatar.cc/150?u=sarah', true, false, 'sarah.johnson@example.com', NOW() - INTERVAL '90 days', NOW()),
('mock_user_2', 'Michael Chen', 'https://i.pravatar.cc/150?u=michael', false, false, 'michael.chen@example.com', NOW() - INTERVAL '85 days', NOW()),
('mock_user_3', 'Emily Rodriguez', 'https://i.pravatar.cc/150?u=emily', true, false, 'emily.rodriguez@example.com', NOW() - INTERVAL '80 days', NOW()),
('mock_user_4', 'James Wilson', 'https://i.pravatar.cc/150?u=james', false, false, 'james.wilson@example.com', NOW() - INTERVAL '75 days', NOW()),
('mock_user_5', 'Jessica Martinez', 'https://i.pravatar.cc/150?u=jessica', false, false, 'jessica.martinez@example.com', NOW() - INTERVAL '70 days', NOW()),
('mock_user_6', 'David Brown', 'https://i.pravatar.cc/150?u=david', false, false, 'david.brown@example.com', NOW() - INTERVAL '65 days', NOW()),
('mock_user_7', 'Amanda Taylor', 'https://i.pravatar.cc/150?u=amanda', false, false, 'amanda.taylor@example.com', NOW() - INTERVAL '60 days', NOW()),
('mock_user_8', 'Christopher Lee', 'https://i.pravatar.cc/150?u=christopher', false, false, 'christopher.lee@example.com', NOW() - INTERVAL '55 days', NOW()),
('mock_user_9', 'Lisa Anderson', 'https://i.pravatar.cc/150?u=lisa', false, false, 'lisa.anderson@example.com', NOW() - INTERVAL '50 days', NOW()),
('mock_user_10', 'Robert Garcia', 'https://i.pravatar.cc/150?u=robert', false, false, 'robert.garcia@example.com', NOW() - INTERVAL '45 days', NOW()),
('mock_user_11', 'Jennifer Kim', 'https://i.pravatar.cc/150?u=jennifer', false, false, 'jennifer.kim@example.com', NOW() - INTERVAL '40 days', NOW()),
('mock_user_12', 'Daniel Patel', 'https://i.pravatar.cc/150?u=daniel', false, false, 'daniel.patel@example.com', NOW() - INTERVAL '35 days', NOW()),
('mock_user_13', 'Michelle White', 'https://i.pravatar.cc/150?u=michelle', false, false, 'michelle.white@example.com', NOW() - INTERVAL '30 days', NOW()),
('mock_user_14', 'Kevin Thompson', 'https://i.pravatar.cc/150?u=kevin', false, false, 'kevin.thompson@example.com', NOW() - INTERVAL '25 days', NOW()),
('mock_user_15', 'Ashley Davis', 'https://i.pravatar.cc/150?u=ashley', false, false, 'ashley.davis@example.com', NOW() - INTERVAL '20 days', NOW()),
('mock_user_16', 'Ryan Martinez', 'https://i.pravatar.cc/150?u=ryan', false, false, 'ryan.martinez@example.com', NOW() - INTERVAL '15 days', NOW()),
('mock_user_17', 'Nicole Harris', 'https://i.pravatar.cc/150?u=nicole', false, false, 'nicole.harris@example.com', NOW() - INTERVAL '10 days', NOW()),
('mock_user_18', 'Brandon Clark', 'https://i.pravatar.cc/150?u=brandon', false, false, 'brandon.clark@example.com', NOW() - INTERVAL '5 days', NOW()),
('mock_user_19', 'Stephanie Lewis', 'https://i.pravatar.cc/150?u=stephanie', false, true, 'stephanie.lewis@example.com', NOW() - INTERVAL '3 days', NOW()),
('mock_user_20', 'Jason Walker', 'https://i.pravatar.cc/150?u=jason', false, false, 'jason.walker@example.com', NOW() - INTERVAL '1 day', NOW());

-- ================================================================================
-- WORKSPACES (5 mock workspaces)
-- ================================================================================
INSERT INTO workspaces (name, slug, description, created_at, updated_at) VALUES
('Tech Innovators Inc', 'tech-innovators', 'Building the future of technology', NOW() - INTERVAL '85 days', NOW()),
('Marketing Wizards', 'marketing-wizards', 'Creative marketing solutions', NOW() - INTERVAL '70 days', NOW()),
('Product Squad', 'product-squad', 'Product development and design', NOW() - INTERVAL '60 days', NOW()),
('Sales Champions', 'sales-champions', 'Crushing sales targets', NOW() - INTERVAL '50 days', NOW()),
('Customer Success Hub', 'customer-success', 'Ensuring customer satisfaction', NOW() - INTERVAL '40 days', NOW());

-- ================================================================================
-- USER_WORKSPACE_MEMBERS (Assign users to workspaces with roles)
-- ================================================================================
-- Tech Innovators Inc (Workspace 1)
INSERT INTO user_workspace_members (user_id, workspace_id, role, joined_at) VALUES
(1, 1, 'OWNER', NOW() - INTERVAL '85 days'),
(2, 1, 'ADMIN', NOW() - INTERVAL '80 days'),
(3, 1, 'MEMBER', NOW() - INTERVAL '75 days'),
(4, 1, 'MEMBER', NOW() - INTERVAL '70 days'),
(5, 1, 'MEMBER', NOW() - INTERVAL '65 days'),
(6, 1, 'MEMBER', NOW() - INTERVAL '60 days');

-- Marketing Wizards (Workspace 2)
INSERT INTO user_workspace_members (user_id, workspace_id, role, joined_at) VALUES
(7, 2, 'OWNER', NOW() - INTERVAL '70 days'),
(8, 2, 'ADMIN', NOW() - INTERVAL '65 days'),
(9, 2, 'MEMBER', NOW() - INTERVAL '60 days'),
(10, 2, 'MEMBER', NOW() - INTERVAL '55 days');

-- Product Squad (Workspace 3)
INSERT INTO user_workspace_members (user_id, workspace_id, role, joined_at) VALUES
(11, 3, 'OWNER', NOW() - INTERVAL '60 days'),
(12, 3, 'MEMBER', NOW() - INTERVAL '55 days'),
(13, 3, 'MEMBER', NOW() - INTERVAL '50 days'),
(14, 3, 'MEMBER', NOW() - INTERVAL '45 days'),
(15, 3, 'MEMBER', NOW() - INTERVAL '40 days');

-- Sales Champions (Workspace 4)
INSERT INTO user_workspace_members (user_id, workspace_id, role, joined_at) VALUES
(16, 4, 'OWNER', NOW() - INTERVAL '50 days'),
(17, 4, 'MEMBER', NOW() - INTERVAL '45 days'),
(18, 4, 'MEMBER', NOW() - INTERVAL '40 days');

-- Customer Success Hub (Workspace 5)
INSERT INTO user_workspace_members (user_id, workspace_id, role, joined_at) VALUES
(3, 5, 'OWNER', NOW() - INTERVAL '40 days'),
(19, 5, 'MEMBER', NOW() - INTERVAL '35 days'),
(20, 5, 'MEMBER', NOW() - INTERVAL '30 days');

-- ================================================================================
-- CHANNELS (15 channels across workspaces)
-- ================================================================================
-- Tech Innovators Inc channels (no updated_at column)
INSERT INTO channels (workspace_id, name, description, created_at) VALUES
(1, 'general', 'General team discussions', NOW() - INTERVAL '85 days'),
(1, 'engineering', 'Engineering team chat', NOW() - INTERVAL '80 days'),
(1, 'product-updates', 'Product announcements', NOW() - INTERVAL '75 days'),

-- Marketing Wizards channels
(2, 'general-marketing', 'Marketing team general chat', NOW() - INTERVAL '70 days'),
(2, 'campaigns', 'Marketing campaign planning', NOW() - INTERVAL '65 days'),
(2, 'social-media', 'Social media discussions', NOW() - INTERVAL '60 days'),

-- Product Squad channels
(3, 'general-product', 'Product team discussions', NOW() - INTERVAL '60 days'),
(3, 'design', 'Design feedback and reviews', NOW() - INTERVAL '55 days'),
(3, 'user-research', 'User research findings', NOW() - INTERVAL '50 days'),

-- Sales Champions channels
(4, 'general-sales', 'Sales team chat', NOW() - INTERVAL '50 days'),
(4, 'deals', 'Deal pipeline discussions', NOW() - INTERVAL '45 days'),

-- Customer Success Hub channels
(5, 'general-support', 'Customer success discussions', NOW() - INTERVAL '40 days'),
(5, 'escalations', 'Customer escalations', NOW() - INTERVAL '35 days'),
(5, 'feedback', 'Customer feedback', NOW() - INTERVAL '30 days');

-- ================================================================================
-- MESSAGES (500+ messages with varying patterns)
-- ================================================================================
-- Generate messages across different time periods to show activity trends

-- Recent activity (last 7 days) - High volume
INSERT INTO messages (channel_id, sender_id, content, type, reply_count, created_at)
SELECT
    (RANDOM() * 13 + 1)::INT as channel_id,
    (RANDOM() * 19 + 1)::INT as sender_id,
    CASE (RANDOM() * 10)::INT
        WHEN 0 THEN 'Great work on the latest release!'
        WHEN 1 THEN 'Can someone review my PR?'
        WHEN 2 THEN 'Meeting in 15 minutes'
        WHEN 3 THEN 'Updated the documentation'
        WHEN 4 THEN 'Thanks for the help!'
        WHEN 5 THEN 'What is the status on this?'
        WHEN 6 THEN 'Here are the analytics for this week'
        WHEN 7 THEN 'Let us discuss this tomorrow'
        WHEN 8 THEN 'I will take a look at this'
        ELSE 'Sounds good!'
    END as content,
    'TEXT' as type,
    0 as reply_count,
    NOW() - (RANDOM() * INTERVAL '7 days') as created_at
FROM generate_series(1, 200);

-- Medium activity (8-30 days ago)
INSERT INTO messages (channel_id, sender_id, content, type, reply_count, created_at)
SELECT
    (RANDOM() * 13 + 1)::INT,
    (RANDOM() * 19 + 1)::INT,
    'Message from ' || ((RANDOM() * 30 + 8)::INT) || ' days ago',
    'TEXT',
    0,
    NOW() - ((RANDOM() * 22 + 8) * INTERVAL '1 day')
FROM generate_series(1, 150);

-- Historical data (31-90 days ago)
INSERT INTO messages (channel_id, sender_id, content, type, reply_count, created_at)
SELECT
    (RANDOM() * 13 + 1)::INT,
    (RANDOM() * 19 + 1)::INT,
    'Historical message',
    'TEXT',
    0,
    NOW() - ((RANDOM() * 60 + 30) * INTERVAL '1 day')
FROM generate_series(1, 200);

-- ================================================================================
-- DIRECT MESSAGES (100 DMs)
-- ================================================================================
INSERT INTO direct_messages (sender_id, recipient_id, content, type, created_at)
SELECT
    (RANDOM() * 19 + 1)::INT as sender_id,
    CASE
        WHEN (RANDOM() * 19 + 1)::INT = (RANDOM() * 19 + 1)::INT
        THEN (RANDOM() * 19 + 1)::INT
        ELSE (RANDOM() * 19 + 1)::INT
    END as recipient_id,
    'Direct message: ' || generate_series,
    'TEXT',
    NOW() - (RANDOM() * INTERVAL '60 days')
FROM generate_series(1, 100)
WHERE (RANDOM() * 19 + 1)::INT <= 20;

-- ================================================================================
-- REACTIONS (300 reactions on messages)
-- Reactions table uses 'type' column with CHECK constraint (only 'THUMBS_UP' allowed)
-- ================================================================================
INSERT INTO reactions (message_id, user_id, type)
SELECT
    m.id as message_id,
    (RANDOM() * 19 + 1)::INT as user_id,
    'THUMBS_UP' as type
FROM messages m
ORDER BY RANDOM()
LIMIT 300
ON CONFLICT (message_id, user_id) DO NOTHING;

-- ================================================================================
-- NOTIFICATIONS (200 notifications)
-- ================================================================================
INSERT INTO notifications (user_id, type, message, is_read, channel_id, message_id, created_at)
SELECT
    (RANDOM() * 19 + 1)::INT as user_id,
    CASE (RANDOM() * 4)::INT
        WHEN 0 THEN 'CHANNEL_MESSAGE'
        WHEN 1 THEN 'DIRECT_MESSAGE'
        WHEN 2 THEN 'MENTION'
        WHEN 3 THEN 'REACTION'
        ELSE 'THREAD_REPLY'
    END::VARCHAR as type,
    'You have a new notification',
    RANDOM() > 0.3 as is_read,
    (RANDOM() * 13 + 1)::INT as channel_id,
    m.id as message_id,
    NOW() - (RANDOM() * INTERVAL '60 days')
FROM messages m
ORDER BY RANDOM()
LIMIT 200;

-- Print summary
SELECT 'Mock data loaded successfully!' as status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as workspace_count FROM workspaces;
SELECT COUNT(*) as channel_count FROM channels;
SELECT COUNT(*) as message_count FROM messages;
SELECT COUNT(*) as dm_count FROM direct_messages;
SELECT COUNT(*) as reaction_count FROM reactions;
SELECT COUNT(*) as notification_count FROM notifications;
