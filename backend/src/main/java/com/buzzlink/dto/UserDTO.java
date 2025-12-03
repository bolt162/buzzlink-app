package com.buzzlink.dto;

import com.buzzlink.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User information sent to frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String clerkId;
    private String displayName;
    private String avatarUrl;
    private Boolean isAdmin;
    private String email;

    /**
     * Convert User entity to DTO
     */
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getId(),
            user.getClerkId(),
            user.getDisplayName(),
            user.getAvatarUrl(),
            user.getIsAdmin(),
            user.getEmail()
        );
    }
}
