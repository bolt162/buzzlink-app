package com.buzzlink.service;

import com.buzzlink.entity.User;
import com.buzzlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    @Lazy
    @Autowired
    private InvitationService invitationService;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Find user by Clerk ID
     */
    public Optional<User> findByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId);
    }

    /**
     * Create or update user from Clerk authentication
     * Called when a user logs in for the first time or updates their profile
     */
    @Transactional
    public User createOrUpdateUser(String clerkId, String displayName, String email, String avatarUrl) {
        Optional<User> existingUser = userRepository.findByClerkId(clerkId);

        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            user.setDisplayName(displayName);
            user.setEmail(email);
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
            return userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User();
            newUser.setClerkId(clerkId);
            newUser.setDisplayName(displayName);
            newUser.setEmail(email);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setIsAdmin(false); // Default to non-admin
            User savedUser = userRepository.save(newUser);

            // Auto-accept any pending invitations for this email
            if (invitationService != null) {
                invitationService.acceptPendingInvitations(email, clerkId);
            }

            return savedUser;
        }
    }

    /**
     * Update user profile (display name, avatar)
     */
    @Transactional
    public User updateProfile(String clerkId, String displayName, String avatarUrl) {
        User user = userRepository.findByClerkId(clerkId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        return userRepository.save(user);
    }

    /**
     * Check if a user is an admin
     */
    public boolean isAdmin(String clerkId) {
        return userRepository.findByClerkId(clerkId)
            .map(User::getIsAdmin)
            .orElse(false);
    }

    /**
     * Make a user an admin (for demo/testing purposes)
     * In production, this would have proper authorization
     */
    @Transactional
    public void setAdmin(String clerkId, boolean isAdmin) {
        User user = userRepository.findByClerkId(clerkId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsAdmin(isAdmin);
        userRepository.save(user);
    }

    /**
     * Get all users (for debugging/admin)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Search users by display name or email
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.searchUsers(query.trim());
    }
}
