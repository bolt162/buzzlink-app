package com.buzzlink.websocket;

import com.buzzlink.dto.DirectMessageDTO;
import com.buzzlink.dto.MessageDTO;
import com.buzzlink.entity.Message;
import com.buzzlink.repository.MessageRepository;
import com.buzzlink.repository.UserWorkspaceMemberRepository;
import com.buzzlink.service.DirectMessageService;
import com.buzzlink.service.MessageService;
import com.buzzlink.service.NotificationService;
import com.buzzlink.service.PresenceService;
import com.buzzlink.websocket.dto.ChatMessage;
import com.buzzlink.websocket.dto.PresenceEvent;
import com.buzzlink.websocket.dto.TypingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket controller for real-time messaging
 * Handles incoming WebSocket messages and broadcasts to subscribers
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final DirectMessageService directMessageService;
    private final PresenceService presenceService;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;
    private final UserWorkspaceMemberRepository workspaceMemberRepository;

    /**
     * Handle incoming chat messages from clients
     * Client sends to: /app/chat.sendMessage
     * Broadcasts to: /topic/channel.{channelId}
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request) {
        log.info("Received message from {}: {}", request.clerkId(), request.content());

        try {
            // Save message to database
            Message.MessageType type = Message.MessageType.valueOf(request.type());
            MessageDTO savedMessage = messageService.createMessage(
                    request.channelId(),
                    request.clerkId(),
                    request.content(),
                    type,
                    request.parentMessageId() // Support threading
            );

            // Convert to WebSocket message format
            ChatMessage chatMessage = new ChatMessage(
                    savedMessage.getId(),
                    savedMessage.getChannelId(),
                    savedMessage.getSender(),
                    savedMessage.getContent(),
                    savedMessage.getType(),
                    savedMessage.getCreatedAt(),
                    savedMessage.getReactionCount(),
                    savedMessage.getParentMessageId(),
                    savedMessage.getReplyCount());

            // Broadcast to all subscribers of this channel
            messagingTemplate.convertAndSend(
                    "/topic/channel." + request.channelId(),
                    chatMessage);

            // Create notifications for workspace members
            try {
                Message message = messageRepository.findById(savedMessage.getId()).orElse(null);
                if (message != null) {
                    // Get all workspace members for this channel
                    Long workspaceId = message.getChannel().getWorkspace().getId();
                    List<String> memberClerkIds = workspaceMemberRepository
                            .findByWorkspaceId(workspaceId)
                            .stream()
                            .map(member -> member.getUser().getClerkId())
                            .collect(Collectors.toList());

                    // Check if it's a thread reply or regular message
                    if (request.parentMessageId() != null) {
                        // It's a reply - notify the parent message author
                        Message parentMessage = messageRepository.findById(request.parentMessageId()).orElse(null);
                        if (parentMessage != null) {
                            notificationService.createThreadReplyNotification(
                                    message,
                                    parentMessage.getSender().getClerkId()
                            );
                        }
                    } else {
                        // Regular channel message - notify all workspace members
                        notificationService.createChannelMessageNotification(message, memberClerkIds);
                    }
                }
            } catch (Exception notifEx) {
                log.warn("Failed to create notification: {}", notifEx.getMessage());
            }

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle typing indicators
     * Client sends to: /app/chat.typing
     * Broadcasts to: /topic/channel.{channelId}.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/channel." + event.getChannelId() + ".typing",
                event);
    }

    /**
     * Handle user joining a channel
     * Client sends to: /app/chat.join
     * Broadcasts presence update to: /topic/channel.{channelId}.presence
     */
    @MessageMapping("/chat.join")
    public void handleJoin(@Payload JoinChannelRequest request) {
        log.info("User {} joined channel {}", request.clerkId(), request.channelId());

        presenceService.userJoined(request.channelId(), request.clerkId());

        // Broadcast updated presence
        broadcastPresence(request.channelId());
    }

    /**
     * Handle user leaving a channel
     * Client sends to: /app/chat.leave
     * Broadcasts presence update to: /topic/channel.{channelId}.presence
     */
    @MessageMapping("/chat.leave")
    public void handleLeave(@Payload LeaveChannelRequest request) {
        log.info("User {} left channel {}", request.clerkId(), request.channelId());

        presenceService.userLeft(request.channelId(), request.clerkId());

        // Broadcast updated presence
        broadcastPresence(request.channelId());
    }

    /**
     * Broadcast presence update for a channel
     */
    private void broadcastPresence(Long channelId) {
        PresenceEvent presenceEvent = new PresenceEvent(
                channelId,
                presenceService.getOnlineUsers(channelId),
                presenceService.getOnlineCount(channelId));

        messagingTemplate.convertAndSend(
                "/topic/channel." + channelId + ".presence",
                presenceEvent);
    }

    /**
     * Handle sending direct messages
     * Client sends to: /app/dm.send
     * Broadcasts to: /topic/dm.{clerkId} for both sender and recipient
     */
    @MessageMapping("/dm.send")
    public void sendDirectMessage(@Payload SendDirectMessageRequest request) {
        log.info("Received DM from {} to user {}", request.senderClerkId(), request.recipientId());

        try {
            // Save DM to database
            DirectMessageDTO dm = directMessageService.sendDirectMessage(
                    request.senderClerkId(),
                    request.recipientId(),
                    request.content(),
                    request.type());

            log.info("Saved DM with ID: {}, broadcasting to sender {} and recipient {}",
                    dm.id(), dm.sender().getClerkId(), dm.recipient().getClerkId());

            // Send to recipient's personal topic
            messagingTemplate.convertAndSend(
                    "/topic/dm." + dm.recipient().getClerkId(),
                    dm);
            log.debug("Sent DM to recipient topic: /topic/dm.{}", dm.recipient().getClerkId());

            // Also send back to sender for confirmation
            messagingTemplate.convertAndSend(
                    "/topic/dm." + dm.sender().getClerkId(),
                    dm);
            log.debug("Sent DM to sender topic: /topic/dm.{}", dm.sender().getClerkId());

            // Create notification for DM
            try {
                notificationService.createDirectMessageNotification(
                        request.senderClerkId(),
                        dm.recipient().getClerkId(),
                        dm.id()
                );
            } catch (Exception notifEx) {
                log.warn("Failed to create DM notification: {}", notifEx.getMessage());
            }

        } catch (Exception e) {
            log.error("Error sending DM: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle DM typing indicators
     * Client sends to: /app/dm.typing
     * Broadcasts to: /topic/dm.{recipientClerkId}.typing
     */
    @MessageMapping("/dm.typing")
    public void handleDMTyping(@Payload DMTypingRequest request) {
        log.info("Received DM typing from {} to {}: {}", request.senderClerkId(), request.recipientClerkId(), request.isTyping());

        try {
            // Create typing event
            TypingEvent typingEvent = new TypingEvent(
                    null, // channelId is null for DMs
                    request.senderClerkId(),
                    request.displayName(),
                    request.isTyping()
            );

            // Send to recipient's personal DM typing topic
            messagingTemplate.convertAndSend(
                    "/topic/dm." + request.recipientClerkId() + ".typing",
                    typingEvent
            );

            log.debug("Sent DM typing event to topic: /topic/dm.{}.typing", request.recipientClerkId());
        } catch (Exception e) {
            log.error("Error handling DM typing: {}", e.getMessage(), e);
        }
    }

    /**
     * Request records
     */
    public record SendMessageRequest(Long channelId, String clerkId, String content, String type,
            Long parentMessageId) {
    }

    public record SendDirectMessageRequest(String senderClerkId, Long recipientId, String content, String type) {
    }

    public record JoinChannelRequest(Long channelId, String clerkId) {
    }

    public record LeaveChannelRequest(Long channelId, String clerkId) {
    }

    public record DMTypingRequest(String senderClerkId, String recipientClerkId, String displayName, boolean isTyping) {
    }
}
