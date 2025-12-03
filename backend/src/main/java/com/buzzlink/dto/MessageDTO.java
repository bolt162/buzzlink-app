package com.buzzlink.dto;

import com.buzzlink.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Message information with sender details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long channelId;
    private UserDTO sender;
    private String content;
    private String type; // TEXT or FILE
    private LocalDateTime createdAt;
    private Long reactionCount;
    private Long parentMessageId; // For threaded replies
    private Integer replyCount; // Number of replies to this message

    /**
     * Convert Message entity to DTO
     */
    public static MessageDTO fromEntity(Message message, Long reactionCount) {
        return new MessageDTO(
                message.getId(),
                message.getChannel().getId(),
                UserDTO.fromEntity(message.getSender()),
                message.getContent(),
                message.getType().name(),
                message.getCreatedAt(),
                reactionCount,
                message.getParentMessage() != null ? message.getParentMessage().getId() : null,
                message.getReplyCount());
    }

    /**
     * Overload for when reaction count is not needed
     */
    public static MessageDTO fromEntity(Message message) {
        return fromEntity(message, 0L);
    }
}
