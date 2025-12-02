export interface User {
  id: number;
  clerkId: string;
  displayName: string;
  avatarUrl?: string;
  isAdmin: boolean;
  email?: string;
}

export interface Channel {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
}

export interface Message {
  id: number;
  channelId: number;
  sender: User;
  content: string;
  type: 'TEXT' | 'FILE';
  createdAt: string;
  reactionCount: number;
  parentMessageId?: number; // For threaded replies
  replyCount: number; // Number of replies to this message
}

export interface TypingEvent {
  channelId: number;
  clerkId: string;
  displayName: string;
  isTyping: boolean;
}

export interface PresenceEvent {
  channelId: number;
  onlineUsers: string[];
  onlineCount: number;
}

export interface Workspace {
  id: number;
  name: string;
  slug: string;
  description: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
  createdAt: string;
}

export interface DirectMessage {
  id: number;
  sender: User;
  recipient: User;
  content: string;
  type: 'TEXT' | 'FILE';
  createdAt: string;
}

export interface Conversation {
  otherUser: User;
  lastMessage: DirectMessage | null;
  unreadCount: number;
}

export interface Notification {
  id: number;
  type: 'CHANNEL_MESSAGE' | 'DIRECT_MESSAGE' | 'THREAD_REPLY' | 'REACTION' | 'MENTION' | 'WORKSPACE_INVITE';
  message: string;
  actor: {
    id: number;
    clerkId: string;
    displayName: string;
    avatarUrl?: string;
  } | null;
  channelId?: number;
  messageId?: number;
  directMessageId?: number;
  workspaceId?: number;
  isRead: boolean;
  createdAt: string;
}
