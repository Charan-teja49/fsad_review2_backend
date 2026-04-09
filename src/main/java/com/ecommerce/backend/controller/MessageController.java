package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Message;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.MessageRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/messages — Send a message
     * Body: { receiverId, content }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> body, Authentication authentication) {
        User sender = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = (String) body.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message content is required"));
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content.trim());
        messageRepository.save(msg);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", msg.getId());
        response.put("senderId", sender.getId());
        response.put("senderName", sender.getName());
        response.put("receiverId", receiver.getId());
        response.put("receiverName", receiver.getName());
        response.put("content", msg.getContent());
        response.put("createdAt", msg.getCreatedAt());
        response.put("message", "Message sent successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/messages/conversations — List all conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Map<String, Object>>> getConversations(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Long> partnerIds = messageRepository.findConversationPartnerIds(currentUser.getId());

        List<Map<String, Object>> conversations = partnerIds.stream().map(partnerId -> {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) return null;

            List<Message> msgs = messageRepository.findConversation(currentUser.getId(), partnerId);
            Message lastMsg = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);
            long unread = msgs.stream()
                    .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && m.getIsRead() == 0)
                    .count();

            Map<String, Object> conv = new LinkedHashMap<>();
            conv.put("partnerId", partner.getId());
            conv.put("partnerName", partner.getName());
            conv.put("partnerRole", partner.getRole().name().toLowerCase());
            conv.put("lastMessage", lastMsg != null ? lastMsg.getContent() : "");
            conv.put("lastMessageTime", lastMsg != null ? lastMsg.getCreatedAt() : null);
            conv.put("unreadCount", unread);
            return conv;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return ResponseEntity.ok(conversations);
    }

    /**
     * GET /api/messages/conversation/{userId} — Get messages with a specific user
     */
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<Map<String, Object>> getConversation(@PathVariable Long userId, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        User otherUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> messages = messageRepository.findConversation(currentUser.getId(), userId);

        // Mark messages as read
        messages.stream()
                .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && m.getIsRead() == 0)
                .forEach(m -> {
                    m.setIsRead(1);
                    messageRepository.save(m);
                });

        List<Map<String, Object>> msgList = messages.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSender().getId());
            map.put("senderName", m.getSender().getName());
            map.put("receiverId", m.getReceiver().getId());
            map.put("content", m.getContent());
            map.put("isRead", m.getIsRead() == 1);
            map.put("createdAt", m.getCreatedAt());
            map.put("isMine", m.getSender().getId().equals(currentUser.getId()));
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("messages", msgList);
        response.put("partner", Map.of(
                "id", otherUser.getId(),
                "name", otherUser.getName(),
                "role", otherUser.getRole().name().toLowerCase()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/messages/unread-count — Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        long count = messageRepository.countUnreadByReceiver(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
