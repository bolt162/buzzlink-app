package com.buzzlink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time messaging
 * Uses STOMP protocol over WebSocket
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.websocket.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    /**
     * Configure message broker
     * - /topic is for broadcasting to all subscribers (channel messages)
     * - /queue is for point-to-point messaging (direct messages)
     * - /app is the prefix for messages bound for @MessageMapping methods
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register STOMP endpoints
     * Clients connect to /ws endpoint with SockJS fallback
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }
}
