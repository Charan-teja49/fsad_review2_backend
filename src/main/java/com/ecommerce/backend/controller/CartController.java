package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.Wishlist;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.WishlistRepository;
import com.ecommerce.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    public CartController(CartService cartService, CartItemRepository cartItemRepository,
                          UserRepository userRepository, ProductRepository productRepository,
                          WishlistRepository wishlistRepository) {
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.wishlistRepository = wishlistRepository;
    }

    // Frontend expects: { items: [...], subtotal, itemCount }
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(Authentication authentication) {
        List<CartItem> cartItems = cartService.getCart(authentication.getName());
        List<Map<String, Object>> items = cartItems.stream().map(ci -> {
            Map<String, Object> item = new LinkedHashMap<>();
            Product p = ci.getProduct();
            item.put("id", ci.getId());
            item.put("product_id", p.getId());
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("compare_price", p.getPrice().multiply(new BigDecimal("1.25")));
            item.put("image_url", p.getImageUrls() != null ? p.getImageUrls().split(",")[0].trim() : "");
            item.put("artisan_name", p.getArtisan() != null ? p.getArtisan().getName() : "Unknown");
            item.put("stock", p.getStock());
            item.put("quantity", ci.getQuantity());
            return item;
        }).collect(Collectors.toList());

        BigDecimal subtotal = cartItems.stream()
                .map(ci -> ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", items);
        response.put("subtotal", subtotal);
        response.put("itemCount", cartItems.stream().mapToInt(CartItem::getQuantity).sum());
        return ResponseEntity.ok(response);
    }

    // Frontend sends: POST /cart { product_id, quantity }
    @PostMapping
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long productId = Long.valueOf(body.get("product_id").toString());
        Integer quantity = body.containsKey("quantity") ? Integer.valueOf(body.get("quantity").toString()) : 1;
        CartItem ci = cartService.addToCart(authentication.getName(), productId, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added to cart successfully");
        response.put("cartItemId", ci.getId());
        return ResponseEntity.ok(response);
    }

    // Frontend sends: PUT /cart/{id} { quantity }
    @PutMapping("/{cartItemId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        int newQty = Integer.parseInt(body.get("quantity").toString());
        if (newQty <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cart updated");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId, Authentication authentication) {
        cartService.removeFromCart(authentication.getName(), cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok().build();
    }
}
