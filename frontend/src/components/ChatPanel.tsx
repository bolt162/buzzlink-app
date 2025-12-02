'use client';

import { useEffect, useState, useRef } from 'react';
import { useUser } from '@clerk/nextjs';
import { Channel, Message, TypingEvent, PresenceEvent, Conversation, DirectMessage } from '@/types';
import { getMessages, getConversation } from '@/lib/api';
import { useWebSocket } from '@/hooks/useWebSocket';
import MessageList from './MessageList';
import MessageInput from './MessageInput';
import TypingIndicator from './TypingIndicator';
import PresenceIndicator from './PresenceIndicator';
import ThreadView from './ThreadView';

interface ChatPanelProps {
  channel: Channel | null;
  conversation: Conversation | null;
}

export default function ChatPanel({ channel, conversation }: ChatPanelProps) {
  const { user } = useUser();
  const [messages, setMessages] = useState<Message[]>([]);
  const [dmMessages, setDmMessages] = useState<DirectMessage[]>([]);
  const [typingUsers, setTypingUsers] = useState<Map<string, string>>(new Map());
  const [onlineCount, setOnlineCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [selectedThread, setSelectedThread] = useState<Message | null>(null);
  const [threadReplies, setThreadReplies] = useState<Message[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutsRef = useRef<Map<string, NodeJS.Timeout>>(new Map());
  const selectedThreadRef = useRef<Message | null>(null);

  const { connected, subscribeToChannel, sendMessage, sendDirectMessage, sendTyping, leaveChannel, subscribeToDMs, sendDMTyping, subscribeToDMTyping } = useWebSocket(
    user?.id || null
  );

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, dmMessages]);

  // Load channel messages
  useEffect(() => {
    if (!channel || !connected || !user || conversation) return;

    const loadMessages = async () => {
      setLoading(true);
      try {
        const fetchedMessages = await getMessages(channel.id);
        setMessages(fetchedMessages.reverse()); // Chronological order
        setDmMessages([]); // Clear DM messages when viewing channel
      } catch (error) {
        console.error('Error loading messages:', error);
      } finally {
        setLoading(false);
      }
    };

    loadMessages();

    const handleNewMessage = (message: Message) => {
      // If it's a reply, handle thread updates
      if (message.parentMessageId) {
        // If we're viewing the thread for this reply, add it to thread replies
        // Use ref to get current value without adding to dependencies
        if (selectedThreadRef.current && selectedThreadRef.current.id === message.parentMessageId) {
          setThreadReplies((prevReplies) => {
            // Avoid duplicates
            if (prevReplies.some(r => r.id === message.id)) return prevReplies;
            return [...prevReplies, message];
          });
        }

        // Update the parent message's reply count
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === message.parentMessageId
              ? { ...msg, replyCount: (msg.replyCount || 0) + 1 }
              : msg
          )
        );
      } else {
        // If it's a top-level message, add it to the list
        setMessages((prev) => {
          // Avoid duplicates
          if (prev.some(m => m.id === message.id)) return prev;
          return [...prev, message];
        });
      }
    };

    const handleTyping = (event: TypingEvent) => {
      console.log('Typing event received:', event);
      console.log('Current user.id:', user.id);
      console.log('Are they equal?', event.clerkId === user.id);

      if (event.clerkId === user.id) {
        console.log('Ignoring own typing event');
        return; // Ignore own typing
      }

      const timeout = typingTimeoutsRef.current.get(event.clerkId);
      if (timeout) clearTimeout(timeout);

      if (event.isTyping) {
        console.log(`Adding ${event.displayName} to typing users`);
        setTypingUsers((prev) => {
          const next = new Map(prev);
          next.set(event.clerkId, event.displayName);
          console.log('Updated typing users map:', Array.from(next.entries()));
          return next;
        });

        const newTimeout = setTimeout(() => {
          console.log(`Removing ${event.displayName} from typing users (timeout)`);
          setTypingUsers((prev) => {
            const next = new Map(prev);
            next.delete(event.clerkId);
            return next;
          });
        }, 3000);

        typingTimeoutsRef.current.set(event.clerkId, newTimeout);
      } else {
        console.log(`Removing ${event.displayName} from typing users (stopped typing)`);
        setTypingUsers((prev) => {
          const next = new Map(prev);
          next.delete(event.clerkId);
          return next;
        });
      }
    };

    const handlePresence = (event: PresenceEvent) => {
      setOnlineCount(event.onlineCount);
    };

    subscribeToChannel(channel.id, handleNewMessage, handleTyping, handlePresence);

    return () => {
      leaveChannel(channel.id);
      typingTimeoutsRef.current.forEach(clearTimeout);
      typingTimeoutsRef.current.clear();
    };
  }, [channel, connected, user, conversation]);

  // Load DM conversation and subscribe to new DMs
  useEffect(() => {
    if (!conversation || !connected || !user || channel) return;

    const loadConversation = async () => {
      setLoading(true);
      try {
        const fetchedMessages = await getConversation(user.id, conversation.otherUser.id);
        setDmMessages(fetchedMessages.reverse()); // Chronological order
        setMessages([]); // Clear channel messages when viewing DM
      } catch (error) {
        console.error('Error loading conversation:', error);
      } finally {
        setLoading(false);
      }
    };

    loadConversation();
  }, [conversation, connected, user, channel]);

  // Subscribe to DMs globally (not per conversation)
  useEffect(() => {
    if (!connected || !user) return;

    const handleNewDM = (dm: DirectMessage) => {
      console.log('ChatPanel: Received new DM:', dm);
      console.log('Current conversation:', conversation);

      // Only add DM if it's part of the current conversation
      if (!conversation) {
        console.log('No active conversation, ignoring DM');
        return;
      }

      // Check if this DM is between the current user and the conversation partner
      // The DM is sent to both sender and recipient, so we need to check if either
      // the sender or recipient is the conversation partner
      const isPartOfConversation =
        dm.sender.id === conversation.otherUser.id ||
        dm.recipient.id === conversation.otherUser.id;

      console.log('Is part of current conversation?', isPartOfConversation);

      if (isPartOfConversation) {
        setDmMessages((prev) => {
          // Avoid duplicates
          if (prev.some(msg => msg.id === dm.id)) {
            console.log('DM already in list, skipping');
            return prev;
          }
          console.log('Adding DM to list');
          return [...prev, dm];
        });
      }
    };

    const handleDMTyping = (event: TypingEvent) => {
      console.log('ChatPanel: DM typing event received:', event);
      console.log('Current user.id:', user.id);
      console.log('Are they equal?', event.clerkId === user.id);

      if (event.clerkId === user.id) {
        console.log('Ignoring own DM typing event');
        return;
      }

      const timeout = typingTimeoutsRef.current.get(event.clerkId);
      if (timeout) clearTimeout(timeout);

      if (event.isTyping) {
        console.log(`Adding ${event.displayName} to DM typing users`);
        setTypingUsers((prev) => {
          const next = new Map(prev);
          next.set(event.clerkId, event.displayName);
          console.log('Updated DM typing users map:', Array.from(next.entries()));
          return next;
        });

        const newTimeout = setTimeout(() => {
          console.log(`Removing ${event.displayName} from DM typing users (timeout)`);
          setTypingUsers((prev) => {
            const next = new Map(prev);
            next.delete(event.clerkId);
            return next;
          });
        }, 3000);

        typingTimeoutsRef.current.set(event.clerkId, newTimeout);
      } else {
        console.log(`Removing ${event.displayName} from DM typing users (stopped typing)`);
        setTypingUsers((prev) => {
          const next = new Map(prev);
          next.delete(event.clerkId);
          return next;
        });
      }
    };

    subscribeToDMs(handleNewDM);
    subscribeToDMTyping(handleDMTyping);
  }, [connected, user, conversation, subscribeToDMs, subscribeToDMTyping]);

  const handleSendMessage = (content: string, type: 'TEXT' | 'FILE') => {
    if (channel) {
      sendMessage(channel.id, content, type);
    } else if (conversation && user) {
      sendDirectMessage(user.id, conversation.otherUser.id, content, type);
    }
  };

  const handleTyping = (isTyping: boolean) => {
    if (!user) return;

    if (channel) {
      // Send channel typing event
      sendTyping(channel.id, user.fullName || user.username || 'User', isTyping);
    } else if (conversation) {
      // Send DM typing event
      console.log('Sending DM typing event:', isTyping, 'to:', conversation.otherUser.clerkId);
      sendDMTyping(conversation.otherUser.clerkId, user.fullName || user.username || 'User', isTyping);
    }
  };

  const handleMessageDeleted = (messageId: number) => {
    setMessages((prev) => prev.filter((msg) => msg.id !== messageId));
  };

  const handleReactionToggled = (messageId: number, newCount: number) => {
    setMessages((prev) =>
      prev.map((msg) => (msg.id === messageId ? { ...msg, reactionCount: newCount } : msg))
    );
  };

  const handleReply = (message: Message) => {
    setSelectedThread(message);
    selectedThreadRef.current = message; // Keep ref in sync
    setThreadReplies([]); // Clear previous thread replies
  };

  const handleCloseThread = () => {
    setSelectedThread(null);
    selectedThreadRef.current = null; // Keep ref in sync
    setThreadReplies([]); // Clear thread replies on close
  };

  const handleSendReply = (content: string, type: 'TEXT' | 'FILE') => {
    if (!selectedThread || !channel) return;
    sendMessage(channel.id, content, type, selectedThread.id);
  };

  if (!channel && !conversation) {
    return (
      <div className="flex-1 flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-semibold text-gray-700 mb-2">Welcome to BuzzLink!</h2>
          <p className="text-gray-500">Select a channel or conversation to start chatting</p>
        </div>
      </div>
    );
  }

  // Convert DM messages to Message format for MessageList component
  const displayMessages = conversation
    ? dmMessages.map((dm) => ({
      id: dm.id,
      channelId: 0, // Not applicable for DMs
      sender: dm.sender,
      content: dm.content,
      type: dm.type,
      createdAt: dm.createdAt,
      reactionCount: 0, // DMs don't have reactions yet
      replyCount: 0, // DMs don't have threading yet
    }))
    : messages.filter((msg) => !msg.parentMessageId); // Filter out thread replies from main view

  return (
    <div className="flex-1 flex flex-col bg-white">
      <div className="border-b border-gray-200 px-6 py-4">
        {channel ? (
          <>
            <h2 className="text-xl font-semibold text-gray-800">#{channel.name}</h2>
            {channel.description && <p className="text-sm text-gray-500">{channel.description}</p>}
          </>
        ) : conversation ? (
          <>
            <h2 className="text-xl font-semibold text-gray-800">{conversation.otherUser.displayName}</h2>
            <p className="text-sm text-gray-500">Direct Message</p>
          </>
        ) : null}
      </div>

      {channel && <PresenceIndicator onlineCount={onlineCount} />}

      {loading ? (
        <div className="flex-1 flex items-center justify-center">
          <div className="text-gray-500">Loading messages...</div>
        </div>
      ) : (
        <>
          <div className="flex-1 flex">
            <div className={selectedThread ? "flex-1" : "flex-1"}>
              <MessageList
                messages={displayMessages}
                onMessageDeleted={handleMessageDeleted}
                onReactionToggled={handleReactionToggled}
                onReply={channel ? handleReply : undefined}
                showReplyButton={!!channel && !selectedThread}
                showReactions={!!channel}
              />
              <div ref={messagesEndRef} />
            </div>
            {selectedThread && (
              <ThreadView
                parentMessage={selectedThread}
                onClose={handleCloseThread}
                onSendReply={handleSendReply}
                newReplies={threadReplies}
              />
            )}
          </div>
        </>
      )}

      <TypingIndicator typingUsers={typingUsers} />

      <MessageInput onSendMessage={handleSendMessage} onTyping={handleTyping} />
    </div>
  );
}
