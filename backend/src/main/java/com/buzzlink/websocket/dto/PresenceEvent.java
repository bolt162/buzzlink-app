package com.buzzlink.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * WebSocket message for presence updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceEvent {
    private Long channelId;
    private Set<String> onlineUsers;
    private int onlineCount;
}
