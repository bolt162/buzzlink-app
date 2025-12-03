package com.buzzlink.dto;

/**
 * Represents a DM conversation with another user
 */
public record ConversationDTO(
    UserDTO otherUser,
    DirectMessageDTO lastMessage,
    int unreadCount
) {
}
