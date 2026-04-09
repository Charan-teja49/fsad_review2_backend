package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByArtisanId(Long artisanId);
    List<Coupon> findByArtisanIsNull(); // Platform coupons
}
