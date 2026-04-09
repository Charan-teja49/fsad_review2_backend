package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.Review;
import com.ecommerce.backend.repository.ReviewRepository;
import com.ecommerce.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ReviewRepository reviewRepository;

    public ProductController(ProductService productService, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.reviewRepository = reviewRepository;
    }

    // Frontend expects: { products: [...] }
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search) {
        List<Product> products = productService.getAllProducts();

        // Filter by category
        if (category != null && !category.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        // Search
        if (search != null && !search.isEmpty()) {
            String q = search.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(q) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(q)) ||
                            (p.getCategory() != null && p.getCategory().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // Sort
        if (sort != null) {
            switch (sort) {
                case "price_low":
                    products.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "price_high":
                    products.sort(Comparator.comparing(Product::getPrice).reversed());
                    break;
                case "newest":
                    products.sort(Comparator.comparing(Product::getCreatedAt).reversed());
                    break;
                default:
                    break;
            }
        }

        List<ProductDto> dtos = products.stream().map(this::toDto).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("products", dtos);
        response.put("total", dtos.size());
        return ResponseEntity.ok(response);
    }

    // Categories endpoint
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, String>>> getCategories() {
        List<Product> products = productService.getAllProducts();
        List<Map<String, String>> categories = products.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .map(cat -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("id", cat.toLowerCase().replace(" ", "-"));
                    m.put("name", cat);
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(productService.getProductById(id)));
    }

    @GetMapping("/artisan/{artisanId}")
    public ResponseEntity<List<ProductDto>> getProductsByArtisan(@PathVariable Long artisanId) {
        return ResponseEntity.ok(
                productService.getProductsByArtisan(artisanId).stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    // GET /api/products/artisan — returns products for the currently authenticated artisan
    @GetMapping("/artisan")
    public ResponseEntity<List<ProductDto>> getMyProducts(Authentication authentication) {
        return ResponseEntity.ok(
                productService.getProductsByArtisanEmail(authentication.getName()).stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody Map<String, Object> body, Authentication authentication) {
        Product product = new Product();
        product.setName((String) body.get("name"));
        product.setDescription((String) body.get("description"));
        product.setPrice(new BigDecimal(body.get("price").toString()));
        product.setStock(Integer.parseInt(body.get("stock").toString()));
        
        // Accept either "category" or "category_id" as category name
        String category = body.get("category") != null ? (String) body.get("category") : (String) body.get("category_id");
        product.setCategory(category);
        
        // Accept imageUrls or image_url
        String imageUrls = body.get("imageUrls") != null ? (String) body.get("imageUrls") : (String) body.get("image_url");
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = "https://images.unsplash.com/photo-1513519245088-0e12902e35ca?w=800";
        }
        product.setImageUrls(imageUrls);
        
        // Generate slug
        String slug = product.getName().toLowerCase()
                .replace(" ", "-")
                .replace("&", "and")
                .replaceAll("[^a-z0-9-]", "")
                + "-" + System.currentTimeMillis();
        product.setSlug(slug);

        return ResponseEntity.ok(toDto(productService.createProduct(product, authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication authentication) {
        Product product = new Product();
        if (body.containsKey("name")) product.setName((String) body.get("name"));
        if (body.containsKey("description")) product.setDescription((String) body.get("description"));
        if (body.containsKey("price")) product.setPrice(new BigDecimal(body.get("price").toString()));
        if (body.containsKey("stock")) product.setStock(Integer.parseInt(body.get("stock").toString()));
        
        String category = body.get("category") != null ? (String) body.get("category") : (String) body.get("category_id");
        product.setCategory(category);
        
        String imageUrls = body.get("imageUrls") != null ? (String) body.get("imageUrls") : (String) body.get("image_url");
        product.setImageUrls(imageUrls);
        
        return ResponseEntity.ok(toDto(productService.updateProduct(id, product, authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    // Search endpoint for autocomplete
    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String q) {
        String query = q.toLowerCase();
        List<ProductDto> results = productService.getAllProducts().stream()
                .filter(p -> p.getName().toLowerCase().contains(query) ||
                        (p.getCategory() != null && p.getCategory().toLowerCase().contains(query)))
                .limit(10)
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setCompare_price(p.getPrice().multiply(new BigDecimal("1.25"))); // 25% markup as compare price
        dto.setStock(p.getStock());
        dto.setCategory(p.getCategory() != null ? p.getCategory().toLowerCase().replace(" ", "-") : "");
        dto.setCategory_name(p.getCategory());
        dto.setImageUrls(p.getImageUrls());
        dto.setImage_url(p.getImageUrls() != null ? p.getImageUrls().split(",")[0].trim() : "");
        dto.setArtisan_id(p.getArtisan() != null ? p.getArtisan().getId() : null);
        dto.setArtisan_name(p.getArtisan() != null ? p.getArtisan().getName() : "Unknown Artisan");
        
        // Calculate average rating from reviews
        List<Review> reviews = reviewRepository.findByProductId(p.getId());
        dto.setReview_count(reviews.size());
        dto.setAvg_rating(reviews.isEmpty() ? 4.5 : reviews.stream().mapToInt(Review::getRating).average().orElse(4.5));
        
        dto.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
        return dto;
    }
}
