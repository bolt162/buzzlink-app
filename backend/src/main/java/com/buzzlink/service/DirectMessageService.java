package com.buzzlink.service;

import com.buzzlink.dto.DirectMessageDTO;
import com.buzzlink.dto.ConversationDTO;
import com.buzzlink.dto.UserDTO;
import com.buzzlink.entity.DirectMessage;
import com.buzzlink.entity.User;
import com.buzzlink.repository.DirectMessageRepository;
import com.buzzlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectMessageService {

        @Autowired
        private DirectMessageRepository dmRepository;

        @Autowired
        private UserRepository userRepository;

        /**
         * Send a direct message
         */
        @Transactional
        public DirectMessageDTO sendDirectMessage(String senderClerkId, Long recipientId, String content, String type) {
                User sender = userRepository.findByClerkId(senderClerkId)
                                .orElseThrow(() -> new RuntimeException("Sender not found"));

                User recipient = userRepository.findById(recipientId)
                                .orElseThrow(() -> new RuntimeException("Recipient not found"));

                DirectMessage.MessageType messageType = DirectMessage.MessageType.valueOf(type.toUpperCase());

                DirectMessage dm = new DirectMessage(sender, recipient, content, messageType);
                dm = dmRepository.save(dm);

                return DirectMessageDTO.from(dm);
        }

        /**
         * Get conversation between two users
         */
        public List<DirectMessageDTO> getConversation(String clerkId, Long otherUserId, int limit) {
                User user = userRepository.findByClerkId(clerkId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<DirectMessage> messages = dmRepository.findConversation(
                                user.getId(),
                                otherUserId,
                                PageRequest.of(0, limit));

                return messages.stream()
                                .map(DirectMessageDTO::from)
                                .collect(Collectors.toList());
        }

        /**
         * Get all conversations for a user
         */
        public List<ConversationDTO> getUserConversations(String clerkId) {
                User user = userRepository.findByClerkId(clerkId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<User> conversationPartners = dmRepository.findConversationPartners(user.getId());

                List<ConversationDTO> conversations = new ArrayList<>();
                for (User partner : conversationPartners) {
                        // Get last message with this user
                        List<DirectMessage> recentMessages = dmRepository.findConversation(
                                        user.getId(),
                                        partner.getId(),
                                        PageRequest.of(0, 1));

                        if (!recentMessages.isEmpty()) {
                                DirectMessage lastMessage = recentMessages.get(0);
                                conversations.add(new ConversationDTO(
                                                UserDTO.fromEntity(partner),
                                                DirectMessageDTO.from(lastMessage),
                                                0 // TODO: Implement unread count tracking
                                ));
                        }
                }

                return conversations;
        }

        /**
         * Get recent DMs for a user
         */
        public List<DirectMessageDTO> getRecentDMs(String clerkId, int limit) {
                User user = userRepository.findByClerkId(clerkId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<DirectMessage> messages = dmRepository.findRecentMessages(
                                user.getId(),
                                PageRequest.of(0, limit));

                return messages.stream()
                                .map(DirectMessageDTO::from)
                                .collect(Collectors.toList());
        }
}
