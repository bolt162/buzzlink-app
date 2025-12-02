import axios from 'axios';
import { Channel, Message, User, Workspace, DirectMessage, Conversation, Notification } from '@/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add Clerk user ID to requests
export const setClerkUserId = (clerkId: string) => {
  api.defaults.headers.common['X-Clerk-User-Id'] = clerkId;
};

// Workspace APIs
export const getWorkspaces = async (clerkId: string): Promise<Workspace[]> => {
  const response = await api.get('/api/workspaces', { params: { clerkId } });
  return response.data;
};

export const getWorkspaceBySlug = async (slug: string, clerkId: string): Promise<Workspace> => {
  const response = await api.get(`/api/workspaces/${slug}`, { params: { clerkId } });
  return response.data;
};

export const createWorkspace = async (
  name: string,
  slug: string,
  description: string,
  creatorClerkId: string
): Promise<Workspace> => {
  const response = await api.post('/api/workspaces', {
    name,
    slug,
    description,
    creatorClerkId,
  });
  return response.data;
};

export const getWorkspaceMembers = async (workspaceId: number, clerkId: string): Promise<User[]> => {
  const response = await api.get(`/api/workspaces/${workspaceId}/members`, {
    params: { clerkId },
  });
  return response.data;
};

// Channel APIs
export const getChannels = async (workspaceId?: number): Promise<Channel[]> => {
  const response = await api.get('/api/channels', {
    params: workspaceId ? { workspaceId } : {},
  });
  return response.data;
};

export const getChannel = async (id: number): Promise<Channel> => {
  const response = await api.get(`/api/channels/${id}`);
  return response.data;
};

export const createChannel = async (
  name: string,
  description: string,
  workspaceId: number
): Promise<Channel> => {
  const response = await api.post('/api/channels', {
    name,
    description,
    workspaceId,
  });
  return response.data;
};

// Message APIs
export const getMessages = async (channelId: number, limit = 50): Promise<Message[]> => {
  const response = await api.get(`/api/channels/${channelId}/messages`, {
    params: { limit },
  });
  return response.data;
};

export const deleteMessage = async (messageId: number): Promise<void> => {
  await api.delete(`/api/messages/${messageId}`);
};

export const toggleReaction = async (messageId: number): Promise<{ count: number }> => {
  const response = await api.post(`/api/messages/${messageId}/reactions`);
  return response.data;
};

export const getThreadReplies = async (messageId: number): Promise<Message[]> => {
  const response = await api.get(`/api/messages/${messageId}/replies`);
  return response.data;
};


// User APIs
export const syncUser = async (
  clerkId: string,
  displayName: string,
  email: string,
  avatarUrl?: string
): Promise<User> => {
  const response = await api.post('/api/users/sync', {
    clerkId,
    displayName,
    email,
    avatarUrl,
  });
  return response.data;
};

export const getCurrentUser = async (): Promise<User> => {
  const response = await api.get('/api/users/me');
  return response.data;
};

export const updateProfile = async (
  displayName: string,
  avatarUrl: string
): Promise<User> => {
  const response = await api.put('/api/users/me', {
    displayName,
    avatarUrl,
  });
  return response.data;
};

export const searchUsers = async (query: string): Promise<User[]> => {
  const response = await api.get('/api/users/search', { params: { query } });
  return response.data;
};

export const addWorkspaceMember = async (
  workspaceId: number,
  clerkId: string,
  role: 'OWNER' | 'ADMIN' | 'MEMBER' = 'MEMBER'
): Promise<void> => {
  await api.post(`/api/workspaces/${workspaceId}/members`, {
    clerkId,
    role,
  });
};

// Invitation APIs
export const sendWorkspaceInvitation = async (
  workspaceId: number,
  email: string,
  inviterClerkId: string,
  role: 'OWNER' | 'ADMIN' | 'MEMBER' = 'MEMBER'
): Promise<{ message: string; invitationId: number; status: string }> => {
  const response = await api.post('/api/invitations', {
    workspaceId,
    email,
    inviterClerkId,
    role,
  });
  return response.data;
};

// Direct Message APIs
export const getConversations = async (clerkId: string): Promise<Conversation[]> => {
  const response = await api.get('/api/direct-messages/conversations', { params: { clerkId } });
  return response.data;
};

export const getConversation = async (
  clerkId: string,
  otherUserId: number,
  limit = 50
): Promise<DirectMessage[]> => {
  const response = await api.get(`/api/direct-messages/conversation/${otherUserId}`, {
    params: { clerkId, limit },
  });
  return response.data;
};

export const sendDirectMessage = async (
  senderClerkId: string,
  recipientId: number,
  content: string,
  type: 'TEXT' | 'FILE' = 'TEXT'
): Promise<DirectMessage> => {
  const response = await api.post('/api/direct-messages', {
    senderClerkId,
    recipientId,
    content,
    type,
  });
  return response.data;
};

// Notification APIs
export const getNotifications = async (clerkId: string): Promise<Notification[]> => {
  const response = await api.get('/api/notifications', { params: { clerkId } });
  return response.data;
};

export const getUnreadNotifications = async (clerkId: string): Promise<Notification[]> => {
  const response = await api.get('/api/notifications/unread', { params: { clerkId } });
  return response.data;
};

export const getUnreadNotificationCount = async (clerkId: string): Promise<number> => {
  const response = await api.get<{ count: number }>('/api/notifications/unread/count', { params: { clerkId } });
  return response.data.count;
};

export const markNotificationAsRead = async (notificationId: number, clerkId: string): Promise<void> => {
  await api.put(`/api/notifications/${notificationId}/read`, null, { params: { clerkId } });
};

export const markAllNotificationsAsRead = async (clerkId: string): Promise<void> => {
  await api.put('/api/notifications/read-all', null, { params: { clerkId } });
};

// Admin APIs
export interface AdminUser {
  id: number;
  clerkId: string;
  displayName: string;
  email: string;
  avatarUrl?: string;
  isAdmin: boolean;
  isBanned: boolean;
  createdAt: string;
  messageCount: number;
}

export interface SystemStats {
  totalUsers: number;
  totalWorkspaces: number;
  totalChannels: number;
  totalMessages: number;
  totalDirectMessages: number;
  bannedUsers: number;
  adminUsers: number;
}

export interface LogEntry {
  timestamp: string;
  level: string;
  message: string;
}

export const getAllUsers = async (): Promise<AdminUser[]> => {
  const response = await api.get('/api/admin/users');
  return response.data;
};

export const getSystemStats = async (): Promise<SystemStats> => {
  const response = await api.get('/api/admin/stats');
  return response.data;
};

export const banUser = async (userId: number): Promise<void> => {
  await api.post(`/api/admin/users/${userId}/ban`);
};

export const unbanUser = async (userId: number): Promise<void> => {
  await api.post(`/api/admin/users/${userId}/unban`);
};

export const deleteUser = async (userId: number): Promise<void> => {
  await api.delete(`/api/admin/users/${userId}`);
};

export const toggleAdminStatus = async (userId: number): Promise<void> => {
  await api.post(`/api/admin/users/${userId}/toggle-admin`);
};

export const getLogs = async (limit = 100, level = 'INFO'): Promise<{ logs: LogEntry[]; count: number; level: string }> => {
  const response = await api.get('/api/admin/logs', { params: { limit, level } });
  return response.data;
};

export default api;
