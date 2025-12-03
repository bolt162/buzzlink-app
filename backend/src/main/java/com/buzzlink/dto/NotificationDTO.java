package com.buzzlink.dto;

import com.buzzlink.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String message;
    private UserDTO actor; // The user who triggered the notification
    private Long channelId;
    private Long messageId;
    private Long directMessageId;
    private Long workspaceId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationDTO from(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name());
        dto.setMessage(notification.getMessage());

        if (notification.getActor() != null) {
            dto.setActor(UserDTO.from(notification.getActor()));
        }

        dto.setChannelId(notification.getChannelId());
        dto.setMessageId(notification.getMessageId());
        dto.setDirectMessageId(notification.getDirectMessageId());
        dto.setWorkspaceId(notification.getWorkspaceId());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());

        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String clerkId;
        private String displayName;
        private String avatarUrl;

        public static UserDTO from(com.buzzlink.entity.User user) {
            return new UserDTO(
                user.getId(),
                user.getClerkId(),
                user.getDisplayName(),
                user.getAvatarUrl()
            );
        }
    }
}
