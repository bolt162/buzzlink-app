package com.buzzlink.service;

import com.buzzlink.dto.MessageDTO;
import com.buzzlink.entity.Channel;
import com.buzzlink.entity.Message;
import com.buzzlink.entity.User;
import com.buzzlink.repository.ChannelRepository;
import com.buzzlink.repository.MessageRepository;
import com.buzzlink.repository.ReactionRepository;
import com.buzzlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing messages
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final NotificationService notificationService;

    /**
     * Get recent messages for a channel
     * 
     * @param channelId Channel ID
     * @param limit     Maximum number of messages to return
     * @return List of messages with sender info and reaction counts
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getRecentMessages(Long channelId, int limit) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        List<Message> messages = messageRepository.findTopLevelMessagesByChannel(
                channel,
                PageRequest.of(0, limit));

        // Reverse to get chronological order (oldest first)
        return messages.stream()
                .map(msg -> {
                    long reactionCount = reactionRepository.countByMessage(msg);
                    return MessageDTO.fromEntity(msg, reactionCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Create a new message
     * 
     * @param channelId Channel ID
     * @param clerkId   Clerk user ID of sender
     * @param content   Message content
     * @param type      Message type (TEXT or FILE)
     * @return Created message DTO
     */
    @Transactional
    public MessageDTO createMessage(Long channelId, String clerkId, String content, Message.MessageType type) {
        return createMessage(channelId, clerkId, content, type, null);
    }

    /**
     * Create a new message with optional parent (for threading)
     * 
     * @param channelId       Channel ID
     * @param clerkId         Clerk user ID of sender
     * @param content         Message content
     * @param type            Message type (TEXT or FILE)
     * @param parentMessageId Parent message ID for threaded replies (null for
     *                        top-level)
     * @return Created message DTO
     */
    @Transactional
    public MessageDTO createMessage(Long channelId, String clerkId, String content, Message.MessageType type,
            Long parentMessageId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        User sender = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();
        message.setChannel(channel);
        message.setSender(sender);
        message.setContent(content);
        message.setType(type);

        // Handle threading
        if (parentMessageId != null) {
            Message parentMessage = messageRepository.findById(parentMessageId)
                    .orElseThrow(() -> new RuntimeException("Parent message not found"));
            message.setParentMessage(parentMessage);

            // Increment parent's reply count
            parentMessage.setReplyCount(parentMessage.getReplyCount() + 1);
            messageRepository.save(parentMessage);
        }

        Message savedMessage = messageRepository.save(message);

        // Publish notification event (stub - would go to Kafka in production)
        notificationService.publishMessageNotification(savedMessage);

        return MessageDTO.fromEntity(savedMessage, 0L);
    }

    /**
     * Delete a message (admin only)
     * 
     * @param messageId Message ID
     * @param clerkId   Clerk user ID requesting deletion
     */
    @Transactional
    public void deleteMessage(Long messageId, String clerkId) {
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsAdmin()) {
            throw new RuntimeException("Only admins can delete messages");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        messageRepository.delete(message);
    }

    /**
     * Toggle reaction on a message
     * 
     * @param messageId Message ID
     * @param clerkId   Clerk user ID
     * @return New reaction count
     */
    @Transactional
    public long toggleReaction(Long messageId, String clerkId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already reacted
        var existingReaction = reactionRepository.findByMessageAndUser(message, user);

        if (existingReaction.isPresent()) {
            // Remove reaction (toggle off)
            reactionRepository.delete(existingReaction.get());
        } else {
            // Add reaction (toggle on)
            var reaction = new com.buzzlink.entity.Reaction();
            reaction.setMessage(message);
            reaction.setUser(user);
            reaction.setType(com.buzzlink.entity.Reaction.ReactionType.THUMBS_UP);
            reactionRepository.save(reaction);
        }

        return reactionRepository.countByMessage(message);
    }

    /**
     * Get all replies to a message (thread)
     * 
     * @param messageId Parent message ID
     * @return List of reply messages
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getThreadReplies(Long messageId) {
        Message parentMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        List<Message> replies = messageRepository.findByParentMessageOrderByCreatedAtAsc(parentMessage);

        return replies.stream()
                .map(msg -> {
                    long reactionCount = reactionRepository.countByMessage(msg);
                    return MessageDTO.fromEntity(msg, reactionCount);
                })
                .collect(Collectors.toList());
    }
}
