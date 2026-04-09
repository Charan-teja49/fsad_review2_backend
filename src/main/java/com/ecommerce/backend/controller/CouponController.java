package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Coupon;
import com.ecommerce.backend.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // Artisan management endpoints
    @GetMapping("/artisan/coupons")
    public ResponseEntity<List<Coupon>> getArtisanCoupons(Authentication auth) {
        return ResponseEntity.ok(couponService.getArtisanCoupons(auth.getName()));
    }

    @PostMapping("/artisan/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Map<String, Object> req, Authentication auth) {
        return ResponseEntity.ok(couponService.createCoupon(auth.getName(), req));
    }

    @PutMapping("/artisan/coupons/{id}/toggle")
    public ResponseEntity<Coupon> toggleCoupon(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(couponService.toggleCoupon(id, auth.getName()));
    }

    @DeleteMapping("/artisan/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id, Authentication auth) {
        couponService.deleteCoupon(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    // Customer validation endpoint
    @PostMapping("/coupons/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(@RequestBody Map<String, Object> req) {
        String code = req.get("code").toString();
        BigDecimal subtotal = new BigDecimal(req.get("subtotal").toString());
        return ResponseEntity.ok(couponService.validateCoupon(code, subtotal));
    }
}
