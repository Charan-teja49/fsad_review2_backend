package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.WishlistRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public WishlistController(WishlistRepository wishlistRepository, UserRepository userRepository,
            ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // Frontend expects list of items with product_id, name, image_url,
    // artisan_name, price, compare_price
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getWishlist(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Wishlist> wishlist = wishlistRepository.findByUserId(user.getId());

        List<Map<String, Object>> items = wishlist.stream().map(w -> {
            Map<String, Object> item = new LinkedHashMap<>();
            Product p = w.getProduct();
            item.put("id", w.getId());
            item.put("product_id", p.getId());
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("compare_price", p.getPrice().multiply(new BigDecimal("1.25")));
            item.put("image_url", p.getImageUrls() != null ? p.getImageUrls().split(",")[0].trim() : "");
            item.put("artisan_name", p.getArtisan() != null ? p.getArtisan().getName() : "Unknown");
            item.put("stock", p.getStock());
            item.put("category", p.getCategory());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    // Frontend sends: POST /wishlist { product_id } — toggles add/remove
    @PostMapping
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestBody Map<String, Object> body,
            Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Long productId = Long.valueOf(body.get("product_id").toString());

        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndProductId(user.getId(), productId);

        Map<String, Object> response = new HashMap<>();
        if (existing.isPresent()) {
            // Remove from wishlist
            wishlistRepository.delete(existing.get());
            response.put("added", false);
            response.put("message", "Removed from wishlist");
        } else {
            // Add to wishlist
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            Wishlist w = new Wishlist();
            w.setUser(user);
            w.setProduct(product);
            wishlistRepository.save(w);
            response.put("added", true);
            response.put("message", "Added to wishlist");
        }

        return ResponseEntity.ok(response);
    }

    // Move wishlist item to cart
    @PostMapping("/move-to-cart")
    public ResponseEntity<Map<String, Object>> moveToCart(@RequestBody Map<String, Object> body,
            Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Long wishlistId = Long.valueOf(body.get("wishlist_id").toString());

        Wishlist w = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));

        if (!w.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Add to cart
        Product product = w.getProduct();
        Optional<CartItem> existingCart = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId());
        if (existingCart.isPresent()) {
            CartItem ci = existingCart.get();
            ci.setQuantity(ci.getQuantity() + 1);
            cartItemRepository.save(ci);
        } else {
            CartItem ci = new CartItem();
            ci.setUser(user);
            ci.setProduct(product);
            ci.setQuantity(1);
            cartItemRepository.save(ci);
        }

        // Remove from wishlist
        wishlistRepository.delete(w);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Moved to cart successfully");
        return ResponseEntity.ok(response);
    }

    // Wishlist count for navbar badge
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getWishlistCount(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Wishlist> wishlist = wishlistRepository.findByUserId(user.getId());
        return ResponseEntity.ok(Map.of("count", wishlist.size()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Wishlist w = wishlistRepository.findById(id).orElseThrow();
        if (w.getUser().getId().equals(user.getId())) {
            wishlistRepository.delete(w);
        }
        return ResponseEntity.ok().build();
    }
}
