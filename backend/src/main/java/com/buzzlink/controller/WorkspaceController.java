package com.buzzlink.controller;

import com.buzzlink.dto.WorkspaceDTO;
import com.buzzlink.entity.UserWorkspaceMember;
import com.buzzlink.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Get all workspaces for a user
     */
    @GetMapping
    public ResponseEntity<List<WorkspaceDTO>> getUserWorkspaces(@RequestParam String clerkId) {
        List<WorkspaceDTO> workspaces = workspaceService.getUserWorkspaces(clerkId);
        return ResponseEntity.ok(workspaces);
    }

    /**
     * Get workspace by slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<WorkspaceDTO> getWorkspaceBySlug(
            @PathVariable String slug,
            @RequestParam String clerkId) {
        var workspace = workspaceService.getWorkspaceBySlug(slug);

        // Check if user is a member
        if (!workspaceService.isUserMemberOf(clerkId, workspace.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Get user's role in this workspace
        var members = workspaceService.getWorkspaceMembers(workspace.getId());
        String role = members.stream()
            .filter(m -> m.getUser().getClerkId().equals(clerkId))
            .findFirst()
            .map(m -> m.getRole().name())
            .orElse("MEMBER");

        return ResponseEntity.ok(WorkspaceDTO.from(workspace, role));
    }

    /**
     * Create new workspace
     */
    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String slug = request.get("slug");
            String description = request.get("description");
            // Support both "clerkId" and "creatorClerkId" for backwards compatibility
            String clerkId = request.getOrDefault("creatorClerkId", request.get("clerkId"));

            if (name == null || slug == null || clerkId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Missing required fields: name, slug, or creatorClerkId"));
            }

            WorkspaceDTO workspace = workspaceService.createWorkspace(name, slug, description, clerkId);
            return ResponseEntity.ok(workspace);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            String message = "Workspace with this name or slug already exists";
            if (e.getMessage().contains("slug")) {
                message = "Workspace slug already exists. Please try a different name.";
            } else if (e.getMessage().contains("name")) {
                message = "Workspace name already exists. Please try a different name.";
            }
            return ResponseEntity.badRequest()
                .body(Map.of("message", message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Error creating workspace: " + e.getMessage()));
        }
    }

    /**
     * Add user to workspace
     */
    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable Long workspaceId,
            @RequestBody Map<String, String> request) {
        String clerkId = request.get("clerkId");
        String roleStr = request.getOrDefault("role", "MEMBER");
        UserWorkspaceMember.Role role = UserWorkspaceMember.Role.valueOf(roleStr);

        workspaceService.addUserToWorkspace(workspaceId, clerkId, role);
        return ResponseEntity.ok().build();
    }

    /**
     * Get workspace members (for DM contacts)
     */
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<?> getWorkspaceMembers(
            @PathVariable Long workspaceId,
            @RequestParam String clerkId) {
        // Check if user is a member
        if (!workspaceService.isUserMemberOf(clerkId, workspaceId)) {
            return ResponseEntity.status(403).build();
        }

        var members = workspaceService.getWorkspaceMembers(workspaceId);

        // Convert to DTOs with user info
        var memberDTOs = members.stream()
            .filter(m -> !m.getUser().getClerkId().equals(clerkId)) // Exclude current user
            .map(m -> Map.of(
                "id", m.getUser().getId(),
                "clerkId", m.getUser().getClerkId(),
                "displayName", m.getUser().getDisplayName(),
                "email", m.getUser().getEmail(),
                "avatarUrl", m.getUser().getAvatarUrl() != null ? m.getUser().getAvatarUrl() : "",
                "role", m.getRole().name()
            ))
            .toList();

        return ResponseEntity.ok(memberDTOs);
    }
}
