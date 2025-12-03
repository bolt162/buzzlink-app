package com.buzzlink.dto;

import com.buzzlink.entity.DirectMessage;
import java.time.LocalDateTime;

public record DirectMessageDTO(
    Long id,
    UserDTO sender,
    UserDTO recipient,
    String content,
    String type, // TEXT or FILE
    LocalDateTime createdAt
) {
    public static DirectMessageDTO from(DirectMessage dm) {
        return new DirectMessageDTO(
            dm.getId(),
            UserDTO.fromEntity(dm.getSender()),
            UserDTO.fromEntity(dm.getRecipient()),
            dm.getContent(),
            dm.getType().name(),
            dm.getCreatedAt()
        );
    }
}
