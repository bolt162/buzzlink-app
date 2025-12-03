package com.buzzlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User entity representing a BuzzLink user.
 * Maps to Clerk authentication users via clerkId.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clerk user ID - used to link authenticated Clerk users to our database
     */
    @Column(unique = true, nullable = false)
    private String clerkId;

    @Column(nullable = false)
    private String displayName;

    /**
     * URL to user's avatar image
     */
    private String avatarUrl;

    /**
     * Admin flag - admins can delete messages and perform moderation
     */
    @Column(nullable = false)
    private Boolean isAdmin = false;

    /**
     * Ban flag - banned users cannot send messages or access channels
     */
    @Column(nullable = false)
    private Boolean isBanned = false;

    /**
     * Email from Clerk (optional, for reference)
     */
    private String email;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
