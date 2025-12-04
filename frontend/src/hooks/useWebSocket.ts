import { useEffect, useRef, useState } from 'react';
import { WebSocketClient } from '@/lib/websocket';
import { Message, TypingEvent, PresenceEvent, DirectMessage, Notification } from '@/types';

export const useWebSocket = (clerkId: string | null) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<WebSocketClient | null>(null);

  useEffect(() => {
    if (!clerkId) return;

    const client = new WebSocketClient(clerkId);
    clientRef.current = client;

    client.connect(() => {
      setConnected(true);
    });

    return () => {
      client.disconnect();
      setConnected(false);
    };
  }, [clerkId]);

  const subscribeToChannel = (
    channelId: number,
    onMessage: (message: Message) => void,
    onTyping: (event: TypingEvent) => void,
    onPresence: (event: PresenceEvent) => void
  ) => {
    if (clientRef.current) {
      clientRef.current.subscribeToChannel(channelId, onMessage, onTyping, onPresence);
    }
  };

  const sendMessage = (channelId: number, content: string, type: 'TEXT' | 'FILE' = 'TEXT', parentMessageId?: number) => {
    if (clientRef.current) {
      clientRef.current.sendMessage(channelId, content, type, parentMessageId);
    }
  };

  const sendTyping = (channelId: number, displayName: string, isTyping: boolean) => {
    if (clientRef.current) {
      clientRef.current.sendTyping(channelId, displayName, isTyping);
    }
  };

  const leaveChannel = (channelId: number) => {
    if (clientRef.current) {
      clientRef.current.leaveChannel(channelId);
    }
  };

  const subscribeToDMs = (onDirectMessage: (dm: DirectMessage) => void) => {
    if (clientRef.current) {
      clientRef.current.subscribeToDMs(onDirectMessage);
    }
  };

  const sendDirectMessage = (senderClerkId: string, recipientId: number, content: string, type: 'TEXT' | 'FILE' = 'TEXT') => {
    if (clientRef.current) {
      clientRef.current.sendDirectMessage(recipientId, content, type);
    }
  };

  const subscribeToNotifications = (
    onNotification: (notification: Notification) => void,
    onCountUpdate: (count: number) => void
  ) => {
    if (clientRef.current) {
      clientRef.current.subscribeToNotifications(onNotification, onCountUpdate);
    }
  };

  const sendDMTyping = (recipientClerkId: string, displayName: string, isTyping: boolean) => {
    if (clientRef.current) {
      clientRef.current.sendDMTyping(recipientClerkId, displayName, isTyping);
    }
  };

  const subscribeToDMTyping = (onTyping: (event: TypingEvent) => void) => {
    if (clientRef.current) {
      clientRef.current.subscribeToDMTyping(onTyping);
    }
  };

  return {
    connected,
    subscribeToChannel,
    sendMessage,
    sendTyping,
    leaveChannel,
    subscribeToDMs,
    sendDirectMessage,
    subscribeToNotifications,
    sendDMTyping,
    subscribeToDMTyping,
  };
};
