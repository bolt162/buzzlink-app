package com.buzzlink.controller;

import com.buzzlink.dto.MessageDTO;
import com.buzzlink.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for message operations
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MessageController {

    private final MessageService messageService;

    /**
     * GET /api/channels/{channelId}/messages - Get recent messages for a channel
     * 
     * @param channelId Channel ID
     * @param limit     Maximum number of messages (default 50)
     */
    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "50") int limit) {
        List<MessageDTO> messages = messageService.getRecentMessages(channelId, limit);
        return ResponseEntity.ok(messages);
    }

    /**
     * DELETE /api/messages/{messageId} - Delete a message (admin only)
     * 
     * @param messageId Message ID
     * @param clerkId   Clerk user ID (from header)
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("X-Clerk-User-Id") String clerkId) {
        try {
            messageService.deleteMessage(messageId, clerkId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * POST /api/messages/{messageId}/reactions - Toggle reaction on a message
     * 
     * @param messageId Message ID
     * @param clerkId   Clerk user ID (from header)
     */
    @PostMapping("/messages/{messageId}/reactions")
    public ResponseEntity<ReactionResponse> toggleReaction(
            @PathVariable Long messageId,
            @RequestHeader("X-Clerk-User-Id") String clerkId) {
        long count = messageService.toggleReaction(messageId, clerkId);
        return ResponseEntity.ok(new ReactionResponse(count));
    }

    /**
     * GET /api/messages/{messageId}/replies - Get all replies to a message (thread)
     * 
     * @param messageId Parent message ID
     */
    @GetMapping("/messages/{messageId}/replies")
    public ResponseEntity<List<MessageDTO>> getThreadReplies(@PathVariable Long messageId) {
        List<MessageDTO> replies = messageService.getThreadReplies(messageId);
        return ResponseEntity.ok(replies);
    }

    /**
     * Response for reaction toggle
     */
    public record ReactionResponse(long count) {
    }
}
