package com.buzzlink.controller;

import com.buzzlink.entity.UserWorkspaceMember;
import com.buzzlink.entity.WorkspaceInvitation;
import com.buzzlink.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * POST /api/invitations - Send workspace invitation
     */
    @PostMapping
    public ResponseEntity<?> sendInvitation(@RequestBody InviteRequest request) {
        try {
            WorkspaceInvitation invitation = invitationService.inviteToWorkspace(
                request.workspaceId(),
                request.email(),
                request.inviterClerkId(),
                UserWorkspaceMember.Role.valueOf(request.role() != null ? request.role() : "MEMBER")
            );

            return ResponseEntity.ok(Map.of(
                "message", "Invitation sent successfully",
                "invitationId", invitation.getId(),
                "status", invitation.getStatus().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/invitations/pending - Get pending invitations for an email
     */
    @GetMapping("/pending")
    public ResponseEntity<List<WorkspaceInvitation>> getPendingInvitations(@RequestParam String email) {
        List<WorkspaceInvitation> invitations = invitationService.getPendingInvitations(email);
        return ResponseEntity.ok(invitations);
    }

    /**
     * POST /api/invitations/accept - Accept all pending invitations for a user
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitations(@RequestBody AcceptInvitationsRequest request) {
        try {
            invitationService.acceptPendingInvitations(request.email(), request.clerkId());
            return ResponseEntity.ok(Map.of("message", "Invitations accepted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/invitations/workspace/{workspaceId} - Get all invitations for a workspace
     */
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<WorkspaceInvitation>> getWorkspaceInvitations(@PathVariable Long workspaceId) {
        List<WorkspaceInvitation> invitations = invitationService.getWorkspaceInvitations(workspaceId);
        return ResponseEntity.ok(invitations);
    }

    /**
     * Request DTOs
     */
    public record InviteRequest(
        Long workspaceId,
        String email,
        String inviterClerkId,
        String role
    ) {}

    public record AcceptInvitationsRequest(
        String email,
        String clerkId
    ) {}
}
