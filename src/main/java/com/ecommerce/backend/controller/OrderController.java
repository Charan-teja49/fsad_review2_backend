package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderRequest;
import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest request, Authentication authentication) {
        Order savedOrder = orderService.createOrder(authentication.getName(), request);
        
        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("orderNumber", "ORD-" + savedOrder.getId());
        orderInfo.put("total", savedOrder.getTotalAmount());
        orderInfo.put("paymentMethod", savedOrder.getPaymentMethod());
        orderInfo.put("deliveryDate", LocalDateTime.now().plusDays(5).toString());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order placed successfully");
        response.put("order", orderInfo);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserOrders(@RequestParam(required = false) String status, Authentication authentication) {
        List<Order> orders = orderService.getUserOrders(authentication.getName());

        if (status != null && !status.isEmpty() && !status.equals("all")) {
            orders = orders.stream()
                .filter(o -> {
                    if (status.equals("active")) return !o.getStatus().equals("cancelled") && !o.getStatus().equals("delivered");
                    if (status.equals("past")) return o.getStatus().equals("cancelled") || o.getStatus().equals("delivered");
                    return o.getStatus().equalsIgnoreCase(status);
                })
                .toList();
        }

        List<Map<String, Object>> response = orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("order_number", "ORD-" + order.getId());
            map.put("created_at", order.getCreatedAt().toString());
            map.put("total", order.getTotalAmount());
            map.put("status", order.getStatus());
            map.put("delivery_date", order.getCreatedAt().plusDays(5).toString());
            map.put("cancel_reason", order.getCancelReason());
            map.put("subtotal", order.getTotalAmount());
            map.put("shipping", 0);
            map.put("discount", 0);
            map.put("address_name", order.getAddress().getName());
            map.put("city", order.getAddress().getCity());
            map.put("state", order.getAddress().getState());
            map.put("pincode", order.getAddress().getPincode());
            
            List<Map<String, Object>> items = order.getItems().stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("product_id", item.getProduct().getId());
                itemMap.put("image_url", item.getProduct().getImageUrls());
                itemMap.put("name", item.getProduct().getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                return itemMap;
            }).toList();
            
            map.put("items", items);
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> payload, Authentication authentication) {
        String reason = payload.getOrDefault("reason", "Customer requested");
        try {
            Order canceled = orderService.cancelOrder(id, authentication.getName(), reason);
            return ResponseEntity.ok(Map.of(
                "message", "Order cancelled successfully",
                "status", canceled.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<Map<String, Object>> updateOrderAddress(@PathVariable Long id, @RequestBody Map<String, Long> payload, Authentication authentication) {
        Long newAddressId = payload.get("address_id");
        try {
            Order updated = orderService.updateOrderAddress(id, newAddressId, authentication.getName());
            return ResponseEntity.ok(Map.of(
                "message", "Order address updated successfully",
                "address", updated.getAddress().getName()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
}
