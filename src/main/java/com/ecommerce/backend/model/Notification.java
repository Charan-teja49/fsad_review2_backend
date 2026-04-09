package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String type; // e.g. "order", "product", "system"
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    private Integer isRead = 0; // 0 for false, 1 for true

    @CreationTimestamp
    private LocalDateTime createdAt;
}
