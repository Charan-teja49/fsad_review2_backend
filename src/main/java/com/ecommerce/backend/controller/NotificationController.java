package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Notification;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.NotificationRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<Map<String, Object>> notifList = notifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("type", n.getType());
            map.put("title", n.getTitle());
            map.put("message", n.getMessage());
            map.put("is_read", n.getIsRead() == 1);
            map.put("created_at", n.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        long unreadCount = notifications.stream().filter(n -> n.getIsRead() == 0).count();

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifList);
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        for (Notification n : notifications) {
            if (n.getIsRead() == 0) {
                n.setIsRead(1);
                notificationRepository.save(n);
            }
        }
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n != null && n.getUser().getId().equals(user.getId())) {
            n.setIsRead(1);
            notificationRepository.save(n);
        }
        return ResponseEntity.ok().build();
    }
}
