package com.ecommerce.backend.controller;

import com.ecommerce.backend.service.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> data) {
        try {
            Long amount = Long.parseLong(data.get("amount").toString());
            String currency = data.getOrDefault("currency", "usd").toString();
            return ResponseEntity.ok(stripeService.createPaymentIntent(amount, currency));
        } catch (StripeException | NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
