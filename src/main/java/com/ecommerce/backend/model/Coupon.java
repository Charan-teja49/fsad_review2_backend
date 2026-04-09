package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "discount_type", nullable = false)
    private String discountType; // "percentage" or "fixed"

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_order")
    private BigDecimal minOrder;

    @Column(name = "max_discount")
    private BigDecimal maxDiscount;

    @Column(name = "usage_limit")
    private Integer usageLimit = 100;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id")
    private User artisan; 

    @CreationTimestamp
    private LocalDateTime createdAt;
}
