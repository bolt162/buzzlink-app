package com.buzzlink.websocket.dto;

import com.buzzlink.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket message for chat messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;
    private Long channelId;
    private UserDTO sender;
    private String content;
    private String type; // TEXT or FILE
    private LocalDateTime createdAt;
    private Long reactionCount;
    private Long parentMessageId; // For threaded replies
    private Integer replyCount; // Number of replies to this message
}
