package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Coupon;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CouponRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public CouponService(CouponRepository couponRepository, UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    public List<Coupon> getArtisanCoupons(String email) {
        User artisan = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Artisan not found"));
        return couponRepository.findByArtisanId(artisan.getId());
    }

    @Transactional
    public Coupon createCoupon(String email, Map<String, Object> req) {
        User artisan = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Artisan not found"));
        
        Coupon coupon = new Coupon();
        coupon.setCode(req.get("code").toString().toUpperCase());
        coupon.setDiscountType(req.get("discount_type").toString());
        coupon.setDiscountValue(new BigDecimal(req.get("discount_value").toString()));
        
        if (req.get("min_order") != null && !req.get("min_order").toString().isEmpty()) {
            coupon.setMinOrder(new BigDecimal(req.get("min_order").toString()));
        }
        if (req.get("max_discount") != null && !req.get("max_discount").toString().isEmpty()) {
            coupon.setMaxDiscount(new BigDecimal(req.get("max_discount").toString()));
        }
        if (req.get("usage_limit") != null) {
            coupon.setUsageLimit(Integer.parseInt(req.get("usage_limit").toString()));
        }
        if (req.get("expires_at") != null && !req.get("expires_at").toString().isEmpty()) {
            coupon.setExpiresAt(LocalDateTime.parse(req.get("expires_at").toString() + "T23:59:59"));
        }
        
        coupon.setArtisan(artisan);
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon toggleCoupon(Long id, String email) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
        // Check if the artisan owns this coupon
        if (coupon.getArtisan() != null && !coupon.getArtisan().getEmail().equals(email)) {
            throw new RuntimeException("Not authorized");
        }
        coupon.setActive(!coupon.getActive());
        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id, String email) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
        if (coupon.getArtisan() != null && !coupon.getArtisan().getEmail().equals(email)) {
            throw new RuntimeException("Not authorized");
        }
        couponRepository.delete(coupon);
    }

    public Map<String, Object> validateCoupon(String code, BigDecimal subtotal) {
        Optional<Coupon> opt = couponRepository.findByCode(code.toUpperCase());
        if (opt.isEmpty()) throw new RuntimeException("Coupon not found");
        
        Coupon coupon = opt.get();
        if (!coupon.getActive()) throw new RuntimeException("Coupon is inactive");
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired");
        }
        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit reached");
        }
        if (coupon.getMinOrder() != null && subtotal.compareTo(coupon.getMinOrder()) < 0) {
            throw new RuntimeException("Order amount below minimum: ₹" + coupon.getMinOrder());
        }

        BigDecimal discount = BigDecimal.ZERO;
        if ("percentage".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = subtotal.multiply(coupon.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        return Map.of("code", code, "discount", discount);
    }
}
