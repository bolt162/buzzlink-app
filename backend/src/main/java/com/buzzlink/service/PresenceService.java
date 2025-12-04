package com.buzzlink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking user presence (online/offline) in channels
 * Uses in-memory storage for simplicity (would use Redis in production)
 */
@Service
@Slf4j
public class PresenceService {

    // Map of channelId -> Set of clerkIds (users online in that channel)
    private final Map<Long, Set<String>> channelPresence = new ConcurrentHashMap<>();

    /**
     * Mark a user as online in a channel
     */
    public void userJoined(Long channelId, String clerkId) {
        channelPresence.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet())
            .add(clerkId);
        log.debug("User {} joined channel {}", clerkId, channelId);
    }

    /**
     * Mark a user as offline in a channel
     */
    public void userLeft(Long channelId, String clerkId) {
        Set<String> users = channelPresence.get(channelId);
        if (users != null) {
            users.remove(clerkId);
            log.debug("User {} left channel {}", clerkId, channelId);
        }
    }

    /**
     * Get all online users in a channel
     */
    public Set<String> getOnlineUsers(Long channelId) {
        return new HashSet<>(channelPresence.getOrDefault(channelId, Collections.emptySet()));
    }

    /**
     * Get count of online users in a channel
     */
    public int getOnlineCount(Long channelId) {
        return channelPresence.getOrDefault(channelId, Collections.emptySet()).size();
    }

    /**
     * Remove user from all channels (e.g., on disconnect)
     */
    public void userDisconnected(String clerkId) {
        channelPresence.values().forEach(users -> users.remove(clerkId));
        log.debug("User {} disconnected from all channels", clerkId);
    }
}
