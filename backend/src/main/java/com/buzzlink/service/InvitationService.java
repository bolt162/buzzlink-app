package com.buzzlink.service;

import com.buzzlink.entity.User;
import com.buzzlink.entity.UserWorkspaceMember;
import com.buzzlink.entity.Workspace;
import com.buzzlink.entity.WorkspaceInvitation;
import com.buzzlink.repository.UserRepository;
import com.buzzlink.repository.WorkspaceInvitationRepository;
import com.buzzlink.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final WorkspaceInvitationRepository invitationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final EmailService emailService;

    /**
     * Create and send workspace invitation
     */
    @Transactional
    public WorkspaceInvitation inviteToWorkspace(
        Long workspaceId,
        String email,
        String inviterClerkId,
        UserWorkspaceMember.Role role
    ) {
        // Get workspace and inviter
        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new RuntimeException("Workspace not found"));

        User inviter = userRepository.findByClerkId(inviterClerkId)
            .orElseThrow(() -> new RuntimeException("Inviter not found"));

        // Check if user is already a member
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            if (workspaceService.isUserMemberOf(existingUser.getClerkId(), workspaceId)) {
                throw new RuntimeException("User is already a member of this workspace");
            }
        }

        // Check if there's already a pending invitation
        var existingInvitation = invitationRepository.findByEmailAndWorkspaceIdAndStatus(
            email, workspaceId, WorkspaceInvitation.Status.PENDING
        );

        if (existingInvitation.isPresent()) {
            throw new RuntimeException("An invitation has already been sent to this email for this workspace");
        }

        // Create invitation
        WorkspaceInvitation invitation = new WorkspaceInvitation();
        invitation.setEmail(email.toLowerCase());
        invitation.setWorkspace(workspace);
        invitation.setInviter(inviter);
        invitation.setRole(role);
        invitation.setStatus(WorkspaceInvitation.Status.PENDING);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));

        invitation = invitationRepository.save(invitation);

        // Send email
        emailService.sendWorkspaceInvitation(
            email,
            workspace.getName(),
            inviter.getDisplayName(),
            invitation.getToken()
        );

        // If user exists, add them to workspace immediately
        if (existingUser != null) {
            workspaceService.addUserToWorkspace(workspaceId, existingUser.getClerkId(), role);
            invitation.setStatus(WorkspaceInvitation.Status.ACCEPTED);
            invitationRepository.save(invitation);
        }

        return invitation;
    }

    /**
     * Get pending invitations for an email
     */
    public List<WorkspaceInvitation> getPendingInvitations(String email) {
        return invitationRepository.findByEmailAndStatus(
            email.toLowerCase(),
            WorkspaceInvitation.Status.PENDING
        );
    }

    /**
     * Accept invitation (called when user signs up/logs in)
     */
    @Transactional
    public void acceptPendingInvitations(String email, String clerkId) {
        List<WorkspaceInvitation> pendingInvitations = getPendingInvitations(email);

        for (WorkspaceInvitation invitation : pendingInvitations) {
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                invitation.setStatus(WorkspaceInvitation.Status.EXPIRED);
                invitationRepository.save(invitation);
                continue;
            }

            try {
                workspaceService.addUserToWorkspace(
                    invitation.getWorkspace().getId(),
                    clerkId,
                    invitation.getRole()
                );
                invitation.setStatus(WorkspaceInvitation.Status.ACCEPTED);
                invitationRepository.save(invitation);
            } catch (Exception e) {
                System.err.println("Failed to accept invitation: " + e.getMessage());
            }
        }
    }

    /**
     * Get all invitations for a workspace
     */
    public List<WorkspaceInvitation> getWorkspaceInvitations(Long workspaceId) {
        return invitationRepository.findByWorkspaceId(workspaceId);
    }
}
