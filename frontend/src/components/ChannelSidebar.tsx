'use client';

import { Channel, Workspace, Conversation, User } from '@/types';
import { useState } from 'react';
import { useUser } from '@clerk/nextjs';
import { createWorkspace, createChannel, sendWorkspaceInvitation } from '@/lib/api';

interface ChannelSidebarProps {
  channels: Channel[];
  selectedChannelId: number | null;
  onSelectChannel: (channelId: number) => void;
  workspaces: Workspace[];
  selectedWorkspace: Workspace | null;
  onSelectWorkspace: (workspace: Workspace) => void;
  conversations: Conversation[];
  selectedConversation: Conversation | null;
  onSelectConversation: (conversation: Conversation | null) => void;
  workspaceMembers: User[];
  onWorkspaceCreated: () => void;
  onChannelCreated: () => void;
}

export default function ChannelSidebar({
  channels,
  selectedChannelId,
  onSelectChannel,
  workspaces,
  selectedWorkspace,
  onSelectWorkspace,
  conversations,
  selectedConversation,
  onSelectConversation,
  workspaceMembers,
  onWorkspaceCreated,
  onChannelCreated,
}: ChannelSidebarProps) {
  const { user } = useUser();
  const [showWorkspaceDropdown, setShowWorkspaceDropdown] = useState(false);

  const handleCreateWorkspace = async () => {
    if (!user) return;

    const name = prompt('Enter workspace name:');
    if (!name || !name.trim()) return;

    // Generate unique slug with timestamp
    const baseSlug = name.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
    const slug = `${baseSlug}-${Date.now()}`;
    const description = prompt('Enter workspace description (optional):') || '';

    try {
      await createWorkspace(name.trim(), slug, description, user.id);
      alert('Workspace created successfully!');
      onWorkspaceCreated();
    } catch (error: any) {
      console.error('Error creating workspace:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
      alert(`Failed to create workspace: ${errorMsg}`);
    }
  };

  const handleCreateChannel = async () => {
    if (!selectedWorkspace) {
      alert('Please select a workspace first');
      return;
    }

    const name = prompt('Enter channel name:');
    if (!name || !name.trim()) return;

    const description = prompt('Enter channel description (optional):') || '';

    try {
      await createChannel(name.trim(), description, selectedWorkspace.id);
      alert('Channel created successfully!');
      onChannelCreated();
    } catch (error: any) {
      console.error('Error creating channel:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
      alert(`Failed to create channel: ${errorMsg}`);
    }
  };

  const handleInviteUser = async () => {
    if (!selectedWorkspace || !user) {
      alert('Please select a workspace first');
      return;
    }

    const email = prompt('Enter email address to invite:');
    if (!email || !email.trim()) return;

    // Basic email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.trim())) {
      alert('Please enter a valid email address');
      return;
    }

    try {
      const result = await sendWorkspaceInvitation(
        selectedWorkspace.id,
        email.trim(),
        user.id,
        'MEMBER'
      );

      if (result.status === 'ACCEPTED') {
        alert(`User already exists and has been added to ${selectedWorkspace.name}!`);
      } else {
        alert(`Invitation sent to ${email}!\n\nThey will receive an email and will be automatically added when they sign up or log in.`);
      }
    } catch (error: any) {
      console.error('Error inviting user:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Unknown error';
      alert(`Failed to send invitation: ${errorMsg}`);
    }
  };

  return (
    <div className="w-64 bg-gray-800 text-white flex flex-col h-full">
      {/* Workspace Selector */}
      <div className="p-4 border-b border-gray-700">
        <div className="flex items-center gap-2 mb-2">
          <button
            onClick={() => setShowWorkspaceDropdown(!showWorkspaceDropdown)}
            className="flex-1 text-left px-3 py-2 bg-gray-700 rounded hover:bg-gray-600 transition flex items-center justify-between"
          >
            <span className="font-semibold truncate">
              {selectedWorkspace?.name || 'Select Workspace'}
            </span>
            <svg
              className={`w-4 h-4 transition-transform ${showWorkspaceDropdown ? 'rotate-180' : ''}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          <button
            onClick={handleCreateWorkspace}
            className="px-3 py-2 bg-blue-600 hover:bg-blue-700 rounded transition"
            title="Create workspace"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
          </button>
        </div>
        {selectedWorkspace && (
          <button
            onClick={handleInviteUser}
            className="w-full px-3 py-2 bg-green-600 hover:bg-green-700 rounded transition text-sm flex items-center justify-center gap-2"
            title="Invite user to workspace"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
            Invite User
          </button>
        )}
        <div className="relative">
          {showWorkspaceDropdown && (
            <div className="absolute top-full left-0 right-0 mt-1 bg-gray-700 rounded shadow-lg z-10 max-h-60 overflow-y-auto">
              {workspaces.map((workspace) => (
                <button
                  key={workspace.id}
                  onClick={() => {
                    onSelectWorkspace(workspace);
                    setShowWorkspaceDropdown(false);
                  }}
                  className={`w-full text-left px-3 py-2 hover:bg-gray-600 transition ${selectedWorkspace?.id === workspace.id ? 'bg-gray-600' : ''
                    }`}
                >
                  <div className="font-medium">{workspace.name}</div>
                  <div className="text-xs text-gray-400">{workspace.description}</div>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Channels Section */}
      <div className="flex-1 overflow-y-auto">
        <div className="p-3 flex items-center justify-between">
          <div className="text-xs font-semibold text-gray-400 uppercase">Channels</div>
          <button
            onClick={handleCreateChannel}
            className="p-1 hover:bg-gray-700 rounded transition"
            title="Create channel"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
          </button>
        </div>
        {channels.map((channel) => (
          <button
            key={channel.id}
            onClick={() => {
              onSelectChannel(channel.id);
            }}
            className={`w-full text-left px-4 py-3 hover:bg-gray-700 transition ${selectedChannelId === channel.id && !selectedConversation
              ? 'bg-gray-700 border-l-4 border-blue-500'
              : ''
              }`}
          >
            <div className="flex items-center space-x-2">
              <span className="text-gray-400">#</span>
              <span className="font-medium">{channel.name}</span>
            </div>
            {channel.description && (
              <p className="text-xs text-gray-400 mt-1 ml-5">{channel.description}</p>
            )}
          </button>
        ))}

        {/* Direct Messages Section */}
        <div className="mt-4">
          <div className="p-3 text-xs font-semibold text-gray-400 uppercase">Direct Messages</div>

          {/* Existing Conversations */}
          {conversations.length > 0 && (
            <div>
              {conversations.map((conversation, index) => (
                <button
                  key={`dm-${conversation.otherUser.id}-${index}`}
                  onClick={() => {
                    onSelectConversation(conversation);
                  }}
                  className={`w-full text-left px-4 py-3 hover:bg-gray-700 transition ${selectedConversation?.otherUser.id === conversation.otherUser.id
                    ? 'bg-gray-700 border-l-4 border-green-500'
                    : ''
                    }`}
                >
                  <div className="flex items-center space-x-2">
                    <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                    <span className="font-medium">{conversation.otherUser.displayName}</span>
                  </div>
                  {conversation.lastMessage && (
                    <p className="text-xs text-gray-400 mt-1 ml-5 truncate">
                      {conversation.lastMessage.content}
                    </p>
                  )}
                  {conversation.unreadCount > 0 && (
                    <span className="ml-5 mt-1 inline-block bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                      {conversation.unreadCount}
                    </span>
                  )}
                </button>
              ))}
            </div>
          )}

          {/* Workspace Members */}
          {workspaceMembers.length > 0 && (
            <div className={conversations.length > 0 ? 'mt-2 pt-2 border-t border-gray-700' : ''}>
              <div className="px-3 py-2 text-xs text-gray-500">Workspace Members</div>
              {workspaceMembers.map((member) => {
                // Check if this member already has a conversation
                const hasConversation = conversations.some(
                  (conv) => conv.otherUser.clerkId === member.clerkId
                );

                // Skip if already in conversations
                if (hasConversation) return null;

                return (
                  <button
                    key={`member-${member.id}`}
                    onClick={async () => {
                      if (!user) return;

                      // Start a new conversation by creating a conversation object
                      const newConversation: Conversation = {
                        otherUser: member,
                        lastMessage: null,
                        unreadCount: 0,
                      };
                      onSelectConversation(newConversation);
                    }}
                    className="w-full text-left px-4 py-3 hover:bg-gray-700 transition"
                  >
                    <div className="flex items-center space-x-2">
                      <div className="w-2 h-2 bg-gray-500 rounded-full"></div>
                      <span className="font-medium">{member.displayName}</span>
                    </div>
                    <p className="text-xs text-gray-400 mt-1 ml-5">{member.email}</p>
                  </button>
                );
              })}
            </div>
          )}

          {/* Empty State */}
          {conversations.length === 0 && workspaceMembers.length === 0 && (
            <div className="px-4 py-3 text-sm text-gray-500">No members in this workspace</div>
          )}
        </div>
      </div>
    </div>
  );
}
