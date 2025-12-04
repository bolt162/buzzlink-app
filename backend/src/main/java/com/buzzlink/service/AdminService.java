package com.buzzlink.service;

import com.buzzlink.entity.User;
import com.buzzlink.repository.ChannelRepository;
import com.buzzlink.repository.DirectMessageRepository;
import com.buzzlink.repository.MessageRepository;
import com.buzzlink.repository.UserRepository;
import com.buzzlink.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final DirectMessageRepository directMessageRepository;
    private final ChannelRepository channelRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * Check if user is admin
     */
    public boolean isAdmin(String clerkId) {
        return userRepository.findByClerkId(clerkId)
                .map(User::getIsAdmin)
                .orElse(false);
    }

    /**
     * Get all users with their stats
     */
    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("clerkId", user.getClerkId());
            userMap.put("displayName", user.getDisplayName());
            userMap.put("email", user.getEmail());
            userMap.put("avatarUrl", user.getAvatarUrl());
            userMap.put("isAdmin", user.getIsAdmin());
            userMap.put("isBanned", user.getIsBanned());
            userMap.put("createdAt", user.getCreatedAt());

            // Count messages sent by user
            long messageCount = messageRepository.countBySender(user);
            long dmCount = directMessageRepository.countBySender(user);
            userMap.put("messageCount", messageCount + dmCount);

            return userMap;
        }).toList();
    }

    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalWorkspaces", workspaceRepository.count());
        stats.put("totalChannels", channelRepository.count());
        stats.put("totalMessages", messageRepository.count());
        stats.put("totalDirectMessages", directMessageRepository.count());
        stats.put("bannedUsers", userRepository.findAll().stream().filter(User::getIsBanned).count());
        stats.put("adminUsers", userRepository.findAll().stream().filter(User::getIsAdmin).count());
        return stats;
    }

    /**
     * Ban a user
     */
    @Transactional
    public User banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsBanned(true);
        return userRepository.save(user);
    }

    /**
     * Unban a user
     */
    @Transactional
    public User unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsBanned(false);
        return userRepository.save(user);
    }

    /**
     * Delete a user (soft delete - just ban for demo purposes)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsAdmin()) {
            throw new RuntimeException("Cannot delete admin users");
        }

        // For demo, we'll just ban the user instead of hard delete
        // In production, you'd want to handle cascade deletes or anonymize data
        user.setIsBanned(true);
        userRepository.save(user);
    }

    /**
     * Toggle admin status
     */
    @Transactional
    public User toggleAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsAdmin(!user.getIsAdmin());
        return userRepository.save(user);
    }
}
