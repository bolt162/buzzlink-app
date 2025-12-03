package com.buzzlink.repository;

import com.buzzlink.entity.WorkspaceInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, Long> {

    /**
     * Find invitation by token
     */
    Optional<WorkspaceInvitation> findByToken(String token);

    /**
     * Find pending invitations for an email
     */
    List<WorkspaceInvitation> findByEmailAndStatus(String email, WorkspaceInvitation.Status status);

    /**
     * Find all invitations for a workspace
     */
    List<WorkspaceInvitation> findByWorkspaceId(Long workspaceId);

    /**
     * Find pending invitation by email and workspace
     */
    Optional<WorkspaceInvitation> findByEmailAndWorkspaceIdAndStatus(
        String email,
        Long workspaceId,
        WorkspaceInvitation.Status status
    );
}
