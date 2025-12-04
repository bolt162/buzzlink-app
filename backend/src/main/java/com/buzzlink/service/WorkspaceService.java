package com.buzzlink.service;

import com.buzzlink.dto.WorkspaceDTO;
import com.buzzlink.entity.User;
import com.buzzlink.entity.Workspace;
import com.buzzlink.entity.UserWorkspaceMember;
import com.buzzlink.repository.UserRepository;
import com.buzzlink.repository.WorkspaceRepository;
import com.buzzlink.repository.UserWorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserWorkspaceMemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.buzzlink.repository.ChannelRepository channelRepository;

    /**
     * Get all workspaces a user is a member of
     */
    public List<WorkspaceDTO> getUserWorkspaces(String clerkId) {
        User user = userRepository.findByClerkId(clerkId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserWorkspaceMember> memberships = memberRepository.findByUserId(user.getId());

        return memberships.stream()
            .map(m -> WorkspaceDTO.from(m.getWorkspace(), m.getRole().name()))
            .collect(Collectors.toList());
    }

    /**
     * Get workspace by slug
     */
    public Workspace getWorkspaceBySlug(String slug) {
        return workspaceRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Workspace not found: " + slug));
    }

    /**
     * Get workspace by ID
     */
    public Workspace getWorkspaceById(Long id) {
        return workspaceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Workspace not found: " + id));
    }

    /**
     * Create a new workspace
     */
    @Transactional
    public WorkspaceDTO createWorkspace(String name, String slug, String description, String creatorClerkId) {
        User creator = userRepository.findByClerkId(creatorClerkId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if slug already exists
        if (workspaceRepository.existsBySlug(slug)) {
            throw new RuntimeException("Workspace slug already exists: " + slug);
        }

        // Create workspace
        Workspace workspace = new Workspace(name, slug, description);
        workspace = workspaceRepository.save(workspace);

        // Add creator as OWNER
        UserWorkspaceMember membership = new UserWorkspaceMember(
            creator,
            workspace,
            UserWorkspaceMember.Role.OWNER
        );
        memberRepository.save(membership);

        // Auto-create #general channel
        com.buzzlink.entity.Channel generalChannel = new com.buzzlink.entity.Channel();
        generalChannel.setName("general");
        generalChannel.setDescription("General discussion for " + name);
        generalChannel.setWorkspace(workspace);
        channelRepository.save(generalChannel);

        return WorkspaceDTO.from(workspace, UserWorkspaceMember.Role.OWNER.name());
    }

    /**
     * Add user to workspace
     */
    @Transactional
    public void addUserToWorkspace(Long workspaceId, String clerkId, UserWorkspaceMember.Role role) {
        User user = userRepository.findByClerkId(clerkId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new RuntimeException("Workspace not found"));

        // Check if already a member
        if (memberRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId).isPresent()) {
            throw new RuntimeException("User already a member of this workspace");
        }

        UserWorkspaceMember membership = new UserWorkspaceMember(user, workspace, role);
        memberRepository.save(membership);
    }

    /**
     * Check if user is member of workspace
     */
    public boolean isUserMemberOf(String clerkId, Long workspaceId) {
        User user = userRepository.findByClerkId(clerkId).orElse(null);
        if (user == null) return false;

        return memberRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId).isPresent();
    }

    /**
     * Get all members of a workspace
     */
    public List<UserWorkspaceMember> getWorkspaceMembers(Long workspaceId) {
        return memberRepository.findByWorkspaceId(workspaceId);
    }
}
