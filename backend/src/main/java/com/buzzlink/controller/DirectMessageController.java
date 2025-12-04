package com.buzzlink.controller;

import com.buzzlink.dto.ConversationDTO;
import com.buzzlink.dto.DirectMessageDTO;
import com.buzzlink.service.DirectMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/direct-messages")
@CrossOrigin(origins = "*")
public class DirectMessageController {

    @Autowired
    private DirectMessageService dmService;

    /**
     * Get all conversations for a user
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(@RequestParam String clerkId) {
        List<ConversationDTO> conversations = dmService.getUserConversations(clerkId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get conversation with a specific user
     */
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<DirectMessageDTO>> getConversation(
            @PathVariable Long otherUserId,
            @RequestParam String clerkId,
            @RequestParam(defaultValue = "50") int limit) {
        List<DirectMessageDTO> messages = dmService.getConversation(clerkId, otherUserId, limit);
        return ResponseEntity.ok(messages);
    }

    /**
     * Send a direct message
     */
    @PostMapping
    public ResponseEntity<DirectMessageDTO> sendDirectMessage(@RequestBody Map<String, Object> request) {
        String senderClerkId = (String) request.get("senderClerkId");
        Long recipientId = Long.valueOf(request.get("recipientId").toString());
        String content = (String) request.get("content");
        String type = (String) request.getOrDefault("type", "TEXT");

        DirectMessageDTO dm = dmService.sendDirectMessage(senderClerkId, recipientId, content, type);
        return ResponseEntity.ok(dm);
    }
}
