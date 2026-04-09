package com.ecommerce.backend.controller;

import com.ecommerce.backend.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/api/chat/bot")
    public ResponseEntity<Map<String, String>> chatWithBot(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }
        
        String response = chatbotService.generateResponse(message);
        return ResponseEntity.ok(Map.of("response", response));
    }

    // Alias endpoint - frontend calls /api/chatbot
    @PostMapping("/api/chatbot")
    public ResponseEntity<Map<String, String>> chatWithBotAlias(@RequestBody Map<String, String> request) {
        return chatWithBot(request);
    }
}
