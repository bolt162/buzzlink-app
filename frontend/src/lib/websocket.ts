import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Message, TypingEvent, PresenceEvent, DirectMessage, Notification } from '@/types';

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8080/ws';

export class WebSocketClient {
  private client: Client | null = null;
  private clerkId: string;
  private onMessageCallback?: (message: Message) => void;
  private onTypingCallback?: (event: TypingEvent) => void;
  private onPresenceCallback?: (event: PresenceEvent) => void;
  private onDirectMessageCallback?: (dm: DirectMessage) => void;
  private onNotificationCallback?: (notification: Notification) => void;
  private onNotificationCountCallback?: (count: number) => void;

  constructor(clerkId: string) {
    this.clerkId = clerkId;
  }

  connect(onConnected?: () => void) {
    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL) as any,
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log('WebSocket connected');
      onConnected?.();
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
    };

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }

  subscribeToChannel(
    channelId: number,
    onMessage: (message: Message) => void,
    onTyping: (event: TypingEvent) => void,
    onPresence: (event: PresenceEvent) => void
  ) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    this.onMessageCallback = onMessage;
    this.onTypingCallback = onTyping;
    this.onPresenceCallback = onPresence;

    // Subscribe to messages
    this.client.subscribe(`/topic/channel.${channelId}`, (message) => {
      const chatMessage = JSON.parse(message.body);
      onMessage(chatMessage);
    });

    // Subscribe to typing indicators
    this.client.subscribe(`/topic/channel.${channelId}.typing`, (message) => {
      const typingEvent = JSON.parse(message.body);
      onTyping(typingEvent);
    });

    // Subscribe to presence updates
    this.client.subscribe(`/topic/channel.${channelId}.presence`, (message) => {
      const presenceEvent = JSON.parse(message.body);
      onPresence(presenceEvent);
    });

    // Join the channel
    this.client.publish({
      destination: '/app/chat.join',
      body: JSON.stringify({
        channelId,
        clerkId: this.clerkId,
      }),
    });
  }

  sendMessage(channelId: number, content: string, type: 'TEXT' | 'FILE' = 'TEXT', parentMessageId?: number) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        channelId,
        clerkId: this.clerkId,
        content,
        type,
        parentMessageId: parentMessageId || null,
      }),
    });
  }

  sendTyping(channelId: number, displayName: string, isTyping: boolean) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({
        channelId,
        clerkId: this.clerkId,
        displayName,
        isTyping,
      }),
    });
  }

  leaveChannel(channelId: number) {
    if (!this.client) {
      return;
    }

    this.client.publish({
      destination: '/app/chat.leave',
      body: JSON.stringify({
        channelId,
        clerkId: this.clerkId,
      }),
    });
  }

  subscribeToDMs(onDirectMessage: (dm: DirectMessage) => void) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    this.onDirectMessageCallback = onDirectMessage;

    // Subscribe to user's personal DM topic
    const dmTopic = `/topic/dm.${this.clerkId}`;
    console.log('Subscribing to DM topic:', dmTopic);

    this.client.subscribe(dmTopic, (message) => {
      const dm = JSON.parse(message.body);
      console.log('WebSocket: Received DM via WebSocket:', dm);
      onDirectMessage(dm);
    });
  }

  sendDMTyping(recipientClerkId: string, displayName: string, isTyping: boolean) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: '/app/dm.typing',
      body: JSON.stringify({
        senderClerkId: this.clerkId,
        recipientClerkId,
        displayName,
        isTyping,
      }),
    });
  }

  subscribeToDMTyping(onTyping: (event: TypingEvent) => void) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    // Subscribe to DM typing indicators on user's personal typing topic
    const typingTopic = `/topic/dm.${this.clerkId}.typing`;
    console.log('Subscribing to DM typing topic:', typingTopic);

    this.client.subscribe(typingTopic, (message) => {
      const typingEvent = JSON.parse(message.body);
      console.log('WebSocket: Received DM typing event:', typingEvent);
      onTyping(typingEvent);
    });
  }

  sendDirectMessage(recipientId: number, content: string, type: 'TEXT' | 'FILE' = 'TEXT') {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: '/app/dm.send',
      body: JSON.stringify({
        senderClerkId: this.clerkId,
        recipientId,
        content,
        type,
      }),
    });
  }

  subscribeToNotifications(
    onNotification: (notification: Notification) => void,
    onCountUpdate: (count: number) => void
  ) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    this.onNotificationCallback = onNotification;
    this.onNotificationCountCallback = onCountUpdate;

    // Subscribe to notifications
    this.client.subscribe(`/user/queue/notifications`, (message) => {
      const notification = JSON.parse(message.body);
      onNotification(notification);
    });

    // Subscribe to notification count updates
    this.client.subscribe(`/user/queue/notifications/count`, (message) => {
      const count = JSON.parse(message.body);
      onCountUpdate(count);
    });
  }
}
