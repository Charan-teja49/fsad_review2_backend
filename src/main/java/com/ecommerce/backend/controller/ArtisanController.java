package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/artisan")
public class ArtisanController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public ArtisanController(UserRepository userRepository, ProductRepository productRepository,
                             OrderRepository orderRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * GET /api/artisan/analytics
     * Returns dashboard stats: totals, product count, weekly chart data
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(Authentication authentication) {
        User artisan = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Product> products = productRepository.findByArtisanId(artisan.getId());
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

        // Get all orders that contain this artisan's products
        List<Order> allOrders = orderRepository.findAll();
        List<Order> artisanOrders = allOrders.stream()
                .filter(order -> order.getItems() != null && order.getItems().stream()
                        .anyMatch(item -> productIds.contains(item.getProduct().getId())))
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrderCount = artisanOrders.size();

        for (Order order : artisanOrders) {
            if (!"cancelled".equals(order.getStatus())) {
                for (OrderItem item : order.getItems()) {
                    if (productIds.contains(item.getProduct().getId())) {
                        totalRevenue = totalRevenue.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    }
                }
            }
        }

        Map<String, Object> totals = new HashMap<>();
        totals.put("totalRevenue", totalRevenue);
        totals.put("totalOrders", totalOrderCount);

        // Generate weekly chart data (last 7 days)
        List<Map<String, Object>> weekly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            final LocalDate d = date;
            
            long dayOrders = artisanOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(d))
                    .count();
            
            BigDecimal dayRevenue = artisanOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(d) && !"cancelled".equals(o.getStatus()))
                    .flatMap(o -> o.getItems().stream())
                    .filter(item -> productIds.contains(item.getProduct().getId()))
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("orders", dayOrders);
            dayData.put("revenue", dayRevenue);
            weekly.add(dayData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totals", totals);
        response.put("productCount", products.size());
        response.put("weekly", weekly);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/artisan/products
     * Returns products belonging to the authenticated artisan
     */
    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getArtisanProducts(Authentication authentication) {
        User artisan = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Product> products = productRepository.findByArtisanId(artisan.getId());

        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("description", p.getDescription());
            item.put("price", p.getPrice());
            item.put("stock", p.getStock());
            item.put("category", p.getCategory() != null ? p.getCategory().toLowerCase().replace(" ", "-") : "");
            item.put("category_name", p.getCategory());
            item.put("image_url", p.getImageUrls() != null ? p.getImageUrls().split(",")[0].trim() : "");
            item.put("status", "approved");
            item.put("total_sold", 0);

            List<Review> reviews = reviewRepository.findByProductId(p.getId());
            item.put("avg_rating", reviews.isEmpty() ? 4.5 : reviews.stream().mapToInt(Review::getRating).average().orElse(4.5));
            item.put("review_count", reviews.size());

            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/artisan/orders
     * Returns orders containing the artisan's products
     */
    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getArtisanOrders(Authentication authentication) {
        User artisan = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Product> products = productRepository.findByArtisanId(artisan.getId());
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

        List<Order> allOrders = orderRepository.findAll();
        List<Order> artisanOrders = allOrders.stream()
                .filter(order -> order.getItems() != null && order.getItems().stream()
                        .anyMatch(item -> productIds.contains(item.getProduct().getId())))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());

        List<Map<String, Object>> response = artisanOrders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getId());
            map.put("order_number", "ORD-" + order.getId());
            map.put("created_at", order.getCreatedAt());
            map.put("total", order.getTotalAmount());
            map.put("status", order.getStatus());
            map.put("customer_name", order.getUser().getName());

            List<Map<String, Object>> items = order.getItems().stream()
                    .filter(item -> productIds.contains(item.getProduct().getId()))
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("product_id", item.getProduct().getId());
                        itemMap.put("name", item.getProduct().getName());
                        itemMap.put("image_url", item.getProduct().getImageUrls() != null ? item.getProduct().getImageUrls().split(",")[0].trim() : "");
                        itemMap.put("quantity", item.getQuantity());
                        itemMap.put("price", item.getPrice());
                        return itemMap;
                    }).collect(Collectors.toList());

            map.put("items", items);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/artisan/orders/{id}/status
     * Update order status (shipped, delivered)
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String newStatus = body.get("status");
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);
        orderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order status updated to " + newStatus);
        response.put("status", newStatus);
        return ResponseEntity.ok(response);
    }
}
