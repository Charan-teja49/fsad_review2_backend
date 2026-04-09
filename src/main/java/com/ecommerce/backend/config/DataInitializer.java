package com.ecommerce.backend.config;

import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            JdbcTemplate jdbcTemplate,
            UserRepository userRepository,
            ProductRepository productRepository,
            ReviewRepository reviewRepository,
            CouponRepository couponRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("🌱 Seeding database with dummy data...");
            
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                jdbcTemplate.execute("TRUNCATE TABLE reviews");
                jdbcTemplate.execute("TRUNCATE TABLE wishlist");
                jdbcTemplate.execute("TRUNCATE TABLE cart");
                jdbcTemplate.execute("TRUNCATE TABLE order_items");
                jdbcTemplate.execute("TRUNCATE TABLE orders");
                jdbcTemplate.execute("TRUNCATE TABLE coupons");
                jdbcTemplate.execute("TRUNCATE TABLE products");
                jdbcTemplate.execute("TRUNCATE TABLE users");
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception e) {
                System.out.println("Could not truncate tables perfectly, but will continue. " + e.getMessage());
            }

            // ============ USERS ============
            User admin = new User();
            admin.setName("Platform Admin");
            admin.setEmail("admin@handcraft.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setPhone("9000000001");
            userRepository.save(admin);

            User artisan1 = new User();
            artisan1.setName("Rajesh Kumar");
            artisan1.setEmail("rajesh@artisan.com");
            artisan1.setPassword(passwordEncoder.encode("Artisan@123"));
            artisan1.setRole(Role.ARTISAN);
            artisan1.setPhone("9000000002");
            userRepository.save(artisan1);

            User artisan2 = new User();
            artisan2.setName("Lakshmi Devi");
            artisan2.setEmail("lakshmi@artisan.com");
            artisan2.setPassword(passwordEncoder.encode("Artisan@123"));
            artisan2.setRole(Role.ARTISAN);
            artisan2.setPhone("9000000003");
            userRepository.save(artisan2);

            User artisan3 = new User();
            artisan3.setName("Amit Sharma");
            artisan3.setEmail("amit@artisan.com");
            artisan3.setPassword(passwordEncoder.encode("Artisan@123"));
            artisan3.setRole(Role.ARTISAN);
            artisan3.setPhone("9000000004");
            userRepository.save(artisan3);

            User customer1 = new User();
            customer1.setName("Priya Patel");
            customer1.setEmail("priya@customer.com");
            customer1.setPassword(passwordEncoder.encode("Customer@123"));
            customer1.setRole(Role.CUSTOMER);
            customer1.setPhone("9000000005");
            userRepository.save(customer1);

            User customer2 = new User();
            customer2.setName("Suresh Reddy");
            customer2.setEmail("suresh@customer.com");
            customer2.setPassword(passwordEncoder.encode("Customer@123"));
            customer2.setRole(Role.CUSTOMER);
            customer2.setPhone("9000000006");
            userRepository.save(customer2);

            // ============ PRODUCTS (15 items) ============
            List<Product> productList = new java.util.ArrayList<>();
            productList.add(createProduct("Hand-Carved Wooden Elephant", 
                "Exquisite Sheesham wood carving from Saharanpur.",
                new BigDecimal("2499"), 12, "Wood Craft", artisan1,
                "https://images.unsplash.com/photo-1582555172866-f73bb12a2ab3?w=800"));
            
            productList.add(createProduct("Blue Pottery Vase", 
                "Authentic Jaipur Blue Pottery floral vase.",
                new BigDecimal("1899"), 8, "Pottery", artisan2,
                "https://images.unsplash.com/photo-1612196808214-b7e239e5f6b7?w=800"));
            
            productList.add(createProduct("Madhubani Painting - Tree of Life", 
                "Original Madhubani artwork from Bihar.",
                new BigDecimal("3499"), 5, "Paintings", artisan2,
                "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800"));
            
            productList.add(createProduct("Brass Dhokra Dancing Lady", 
                "Traditional lost-wax metal casting figurine.",
                new BigDecimal("4299"), 6, "Metal Craft", artisan1,
                "https://images.unsplash.com/photo-1567225557594-88d73e55f2cb?w=800"));
            
            productList.add(createProduct("Pashmina Shawl - Kashmir", 
                "Luxurious handwoven Changthangi goat wool shawl.",
                new BigDecimal("8999"), 4, "Textiles", artisan3,
                "https://images.unsplash.com/photo-1601244005535-a48ebfdc1559?w=800"));
            
            productList.add(createProduct("Terracotta Horse - Bankura", 
                "Symbolic folk art pottery from West Bengal.",
                new BigDecimal("1599"), 15, "Pottery", artisan2,
                "https://images.unsplash.com/photo-1604076913837-52ab5f0e2f10?w=800"));
            
            productList.add(createProduct("Sandalwood Carved Box", 
                "Fragranced sandalwood keepsake box from Mysore.",
                new BigDecimal("3299"), 7, "Wood Craft", artisan1,
                "https://images.unsplash.com/photo-1513519245088-0e12902e35ca?w=800"));
            
            productList.add(createProduct("Bidriware Silver Inlay Vase", 
                "14th century metalware heritage from Bidar.",
                new BigDecimal("5499"), 3, "Metal Craft", artisan3,
                "https://images.unsplash.com/photo-1490312278390-ab64016e0aa9?w=800"));
            
            productList.add(createProduct("Chikankari Kurti - White", 
                "Delicate Lucknowi hand-embroidery on fine cotton.",
                new BigDecimal("2199"), 20, "Textiles", artisan3,
                "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800"));
            
            productList.add(createProduct("Warli Art Wall Decor", 
                "Tribal art canvas depicting village life.",
                new BigDecimal("1299"), 10, "Paintings", artisan2,
                "https://images.unsplash.com/photo-1578301978963-f42b5ec0a50b?w=800"));
            
            productList.add(createProduct("Jute Macrame Plant Hanger", 
                "Eco-friendly hand-knotted jute fiber decor.",
                new BigDecimal("799"), 25, "Home Decor", artisan1,
                "https://images.unsplash.com/photo-1622547748225-3fc4abd2cca0?w=800"));
            
            productList.add(createProduct("Channapatna Wooden Toys", 
                "Traditional lacquerware non-toxic wooden toys.",
                new BigDecimal("999"), 18, "Wood Craft", artisan1,
                "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=800"));
            
            productList.add(createProduct("Phulkari Embroidered Dupatta", 
                "Vibrant silk thread embroidery from Punjab.",
                new BigDecimal("1799"), 9, "Textiles", artisan3,
                "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800"));
            
            productList.add(createProduct("Copper Hammered Water Bottle", 
                "99% pure hammered copper for health benefits.",
                new BigDecimal("699"), 30, "Metal Craft", artisan3,
                "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800"));
            
            productList.add(createProduct("Cane & Bamboo Basket Set", 
                "Intricate bamboo weaving from Northeast India.",
                new BigDecimal("1499"), 14, "Home Decor", artisan2,
                "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800"));

            productRepository.saveAll(productList);

            // ============ REVIEWS ============
            List<Product> saved = productRepository.findAll();
            for (int i = 0; i < saved.size(); i++) {
                Product p = saved.get(i);
                // 2-3 reviews per product
                Review r1 = new Review();
                r1.setProduct(p);
                r1.setUser(customer1);
                r1.setRating(4 + (i % 2)); // alternates 4 and 5
                r1.setComment("Beautiful handcrafted piece! The quality is outstanding and it arrived well-packaged.");
                reviewRepository.save(r1);

                Review r2 = new Review();
                r2.setProduct(p);
                r2.setUser(customer2);
                r2.setRating(3 + (i % 3)); // 3, 4, or 5
                r2.setComment("Authentic craftsmanship. Love the intricate details and the story behind it.");
                reviewRepository.save(r2);
            }

            // ============ COUPONS ============
            Coupon c1 = new Coupon();
            c1.setCode("WELCOME10");
            c1.setDiscountType("percentage");
            c1.setDiscountValue(new BigDecimal("10"));
            c1.setMinOrder(new BigDecimal("500"));
            c1.setActive(true);
            couponRepository.save(c1);

            Coupon c2 = new Coupon();
            c2.setCode("SAVER500");
            c2.setDiscountType("fixed");
            c2.setDiscountValue(new BigDecimal("500"));
            c2.setMinOrder(new BigDecimal("3000"));
            c2.setActive(true);
            couponRepository.save(c2);

            System.out.println("✅ Database seeded: " + saved.size() + " products, 2 coupons, 2 customers, 3 artisans, 1 admin");
        };
    }

    private Product createProduct(String name, String desc, BigDecimal price, int stock, String category, User artisan, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(name.toLowerCase().replace(" ", "-").replace("&", "and").replace(",", ""));
        p.setDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        p.setArtisan(artisan);
        p.setImageUrls(imageUrl);
        return p;
    }
}
