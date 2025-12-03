package com.buzzlink.dto;

import com.buzzlink.entity.Workspace;
import java.time.LocalDateTime;

public record WorkspaceDTO(
    Long id,
    String name,
    String slug,
    String description,
    String role, // User's role in this workspace: OWNER, ADMIN, MEMBER
    LocalDateTime createdAt
) {
    public static WorkspaceDTO from(Workspace workspace, String role) {
        return new WorkspaceDTO(
            workspace.getId(),
            workspace.getName(),
            workspace.getSlug(),
            workspace.getDescription(),
            role,
            workspace.getCreatedAt()
        );
    }
}
