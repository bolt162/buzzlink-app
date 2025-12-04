'use client';

import { useEffect, useState } from 'react';
import { useUser } from '@clerk/nextjs';
import { Channel, Workspace, Conversation, User } from '@/types';
import { getChannels, syncUser, setClerkUserId, getWorkspaces, getConversations, getWorkspaceMembers } from '@/lib/api';
import Header from '@/components/Header';
import ChannelSidebar from '@/components/ChannelSidebar';
import ChatPanel from '@/components/ChatPanel';

export default function ChatPage() {
  const { user, isLoaded } = useUser();
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [selectedWorkspace, setSelectedWorkspace] = useState<Workspace | null>(null);
  const [channels, setChannels] = useState<Channel[]>([]);
  const [selectedChannel, setSelectedChannel] = useState<Channel | null>(null);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);
  const [workspaceMembers, setWorkspaceMembers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoaded || !user) return;

    const initialize = async () => {
      try {
        // Sync user with backend
        await syncUser(
          user.id,
          user.fullName || user.username || 'User',
          user.primaryEmailAddress?.emailAddress || '',
          user.imageUrl
        );

        // Set Clerk user ID for API requests
        setClerkUserId(user.id);

        // Load workspaces
        const fetchedWorkspaces = await getWorkspaces(user.id);
        setWorkspaces(fetchedWorkspaces);

        // Select first workspace by default
        if (fetchedWorkspaces.length > 0) {
          setSelectedWorkspace(fetchedWorkspaces[0]);
        }

        // Load conversations
        const fetchedConversations = await getConversations(user.id);
        setConversations(fetchedConversations);
      } catch (error) {
        console.error('Error initializing chat:', error);
      } finally {
        setLoading(false);
      }
    };

    initialize();
  }, [isLoaded, user]);

  // Load channels and workspace members when workspace changes
  useEffect(() => {
    if (!selectedWorkspace || !user) return;

    const loadWorkspaceData = async () => {
      try {
        // Load channels
        const fetchedChannels = await getChannels(selectedWorkspace.id);
        setChannels(fetchedChannels);

        // Select first channel by default
        if (fetchedChannels.length > 0) {
          setSelectedChannel(fetchedChannels[0]);
          setSelectedConversation(null);
        }

        // Load workspace members
        const fetchedMembers = await getWorkspaceMembers(selectedWorkspace.id, user.id);
        setWorkspaceMembers(fetchedMembers);
      } catch (error) {
        console.error('Error loading workspace data:', error);
      }
    };

    loadWorkspaceData();
  }, [selectedWorkspace, user]);

  const handleWorkspaceCreated = async () => {
    if (!user) return;

    // Reload workspaces
    try {
      const fetchedWorkspaces = await getWorkspaces(user.id);
      setWorkspaces(fetchedWorkspaces);

      // Select the newly created workspace (last one)
      if (fetchedWorkspaces.length > 0) {
        setSelectedWorkspace(fetchedWorkspaces[fetchedWorkspaces.length - 1]);
      }
    } catch (error) {
      console.error('Error reloading workspaces:', error);
    }
  };

  const handleChannelCreated = async () => {
    if (!selectedWorkspace) return;

    // Reload channels for current workspace
    try {
      const fetchedChannels = await getChannels(selectedWorkspace.id);
      setChannels(fetchedChannels);

      // Select the newly created channel (last one)
      if (fetchedChannels.length > 0) {
        setSelectedChannel(fetchedChannels[fetchedChannels.length - 1]);
        setSelectedConversation(null);
      }
    } catch (error) {
      console.error('Error reloading channels:', error);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-500">Loading...</div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col">
      <Header />
      <div className="flex-1 flex overflow-hidden">
        <ChannelSidebar
          channels={channels}
          selectedChannelId={selectedChannel?.id || null}
          onSelectChannel={(id) => {
            const channel = channels.find((c) => c.id === id);
            if (channel) {
              setSelectedChannel(channel);
              setSelectedConversation(null); // Clear conversation when selecting channel
            }
          }}
          workspaces={workspaces}
          selectedWorkspace={selectedWorkspace}
          onSelectWorkspace={(workspace) => {
            setSelectedWorkspace(workspace);
          }}
          conversations={conversations}
          selectedConversation={selectedConversation}
          onSelectConversation={(conversation) => {
            setSelectedConversation(conversation);
            if (conversation) {
              setSelectedChannel(null); // Clear channel when selecting conversation
            }
          }}
          workspaceMembers={workspaceMembers}
          onWorkspaceCreated={handleWorkspaceCreated}
          onChannelCreated={handleChannelCreated}
        />
        <ChatPanel
          channel={selectedChannel}
          conversation={selectedConversation}
        />
      </div>
    </div>
  );
}
