package com.buzzlink.service;

import com.buzzlink.dto.NotificationDTO;
import com.buzzlink.entity.Message;
import com.buzzlink.entity.Notification;
import com.buzzlink.entity.User;
import com.buzzlink.repository.NotificationRepository;
import com.buzzlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get all notifications for a user
     */
    public List<NotificationDTO> getUserNotifications(String clerkId) {
        List<Notification> notifications = notificationRepository.findByUserClerkIdOrderByCreatedAtDesc(clerkId);
        return notifications.stream()
                .map(NotificationDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationDTO> getUnreadNotifications(String clerkId) {
        List<Notification> notifications = notificationRepository.findByUserClerkIdAndIsReadFalseOrderByCreatedAtDesc(clerkId);
        return notifications.stream()
                .map(NotificationDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count
     */
    public Long getUnreadCount(String clerkId) {
        return notificationRepository.countByUserClerkIdAndIsReadFalse(clerkId);
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, String clerkId) {
        int updated = notificationRepository.markAsRead(notificationId, clerkId);
        if (updated > 0) {
            // Send updated count via WebSocket
            sendUnreadCountUpdate(clerkId);
        }
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(String clerkId) {
        notificationRepository.markAllAsRead(clerkId);
        sendUnreadCountUpdate(clerkId);
    }

    /**
     * Create a notification for a new channel message
     */
    @Transactional
    public void createChannelMessageNotification(Message message, List<String> recipientClerkIds) {
        User actor = message.getSender();

        for (String recipientClerkId : recipientClerkIds) {
            // Don't notify the sender
            if (recipientClerkId.equals(actor.getClerkId())) {
                continue;
            }

            userRepository.findByClerkId(recipientClerkId).ifPresent(recipient -> {
                Notification notification = new Notification();
                notification.setUser(recipient);
                notification.setType(Notification.NotificationType.CHANNEL_MESSAGE);
                notification.setMessage(actor.getDisplayName() + " posted in a channel");
                notification.setActor(actor);
                notification.setChannelId(message.getChannel().getId());
                notification.setMessageId(message.getId());
                notification.setIsRead(false);

                Notification saved = notificationRepository.save(notification);
                sendNotificationToUser(recipientClerkId, NotificationDTO.from(saved));
            });
        }
    }

    /**
     * Create a notification for a direct message
     */
    @Transactional
    public void createDirectMessageNotification(String senderClerkId, String recipientClerkId, Long dmId) {
        userRepository.findByClerkId(senderClerkId).ifPresent(sender -> {
            userRepository.findByClerkId(recipientClerkId).ifPresent(recipient -> {
                Notification notification = new Notification();
                notification.setUser(recipient);
                notification.setType(Notification.NotificationType.DIRECT_MESSAGE);
                notification.setMessage(sender.getDisplayName() + " sent you a message");
                notification.setActor(sender);
                notification.setDirectMessageId(dmId);
                notification.setIsRead(false);

                Notification saved = notificationRepository.save(notification);
                sendNotificationToUser(recipientClerkId, NotificationDTO.from(saved));
            });
        });
    }

    /**
     * Create a notification for a thread reply
     */
    @Transactional
    public void createThreadReplyNotification(Message reply, String parentMessageAuthorClerkId) {
        User actor = reply.getSender();

        // Don't notify if replying to own message
        if (parentMessageAuthorClerkId.equals(actor.getClerkId())) {
            return;
        }

        userRepository.findByClerkId(parentMessageAuthorClerkId).ifPresent(recipient -> {
            Notification notification = new Notification();
            notification.setUser(recipient);
            notification.setType(Notification.NotificationType.THREAD_REPLY);
            notification.setMessage(actor.getDisplayName() + " replied to your message");
            notification.setActor(actor);
            notification.setChannelId(reply.getChannel().getId());
            notification.setMessageId(reply.getId());
            notification.setIsRead(false);

            Notification saved = notificationRepository.save(notification);
            sendNotificationToUser(parentMessageAuthorClerkId, NotificationDTO.from(saved));
        });
    }

    /**
     * Create a notification for a reaction
     */
    @Transactional
    public void createReactionNotification(String reactorClerkId, String messageAuthorClerkId, Long messageId, Long channelId) {
        // Don't notify if reacting to own message
        if (reactorClerkId.equals(messageAuthorClerkId)) {
            return;
        }

        userRepository.findByClerkId(reactorClerkId).ifPresent(reactor -> {
            userRepository.findByClerkId(messageAuthorClerkId).ifPresent(recipient -> {
                Notification notification = new Notification();
                notification.setUser(recipient);
                notification.setType(Notification.NotificationType.REACTION);
                notification.setMessage(reactor.getDisplayName() + " reacted to your message");
                notification.setActor(reactor);
                notification.setChannelId(channelId);
                notification.setMessageId(messageId);
                notification.setIsRead(false);

                Notification saved = notificationRepository.save(notification);
                sendNotificationToUser(messageAuthorClerkId, NotificationDTO.from(saved));
            });
        });
    }

    /**
     * Send a notification to a user via WebSocket
     */
    private void sendNotificationToUser(String clerkId, NotificationDTO notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    clerkId,
                    "/queue/notifications",
                    notification
            );
            log.info("Sent notification to user {}: {}", clerkId, notification.getMessage());
        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", clerkId, e.getMessage());
        }
    }

    /**
     * Send updated unread count to user via WebSocket
     */
    private void sendUnreadCountUpdate(String clerkId) {
        try {
            Long unreadCount = getUnreadCount(clerkId);
            messagingTemplate.convertAndSendToUser(
                    clerkId,
                    "/queue/notifications/count",
                    unreadCount
            );
        } catch (Exception e) {
            log.error("Error sending unread count to user {}: {}", clerkId, e.getMessage());
        }
    }

    /**
     * Legacy method kept for backward compatibility
     */
    public void publishMessageNotification(Message message) {
        log.info("Message notification published for Message ID: {}, Channel: {}, Sender: {}",
                message.getId(),
                message.getChannel().getName(),
                message.getSender().getDisplayName());
    }

    /**
     * Legacy method kept for backward compatibility
     */
    public void publishMessageDeletedNotification(Long messageId, Long channelId) {
        log.info("Message deleted notification published for Message ID: {}, Channel ID: {}",
                messageId, channelId);
    }

    /**
     * Legacy method kept for backward compatibility
     */
    public void publishMentionNotification(String mentionedUserClerkId, Message message) {
        log.info("Mention notification published for user: {}, Message ID: {}",
                mentionedUserClerkId, message.getId());
    }
}
