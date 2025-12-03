package com.buzzlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * WorkspaceInvitation entity for managing workspace invites
 */
@Entity
@Table(name = "workspace_invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email address of the invited user
     */
    @Column(nullable = false)
    private String email;

    /**
     * Workspace being invited to
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    /**
     * User who sent the invitation
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    /**
     * Role to be assigned when invitation is accepted
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserWorkspaceMember.Role role = UserWorkspaceMember.Role.MEMBER;

    /**
     * Invitation status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    /**
     * Unique token for accepting invitation
     */
    @Column(unique = true, nullable = false)
    private String token;

    /**
     * Expiration date of the invitation
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        DECLINED,
        EXPIRED
    }
}
