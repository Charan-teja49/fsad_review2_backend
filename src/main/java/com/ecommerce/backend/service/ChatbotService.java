package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ProductRepository productRepository;

    public ChatbotService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String generateResponse(String userMessage) {
        String msg = userMessage.toLowerCase().trim();

        try {
            // Greeting
            if (matchesAny(msg, "hello", "hi", "hey", "good morning", "good evening", "good afternoon", "namaste", "howdy", "greetings")) {
                return "Hello! 👋 Welcome to HandCraft! I'm here to help you with:\n\n" +
                       "🛍️ **Product Recommendations** — Ask me about categories or specific crafts\n" +
                       "📦 **Order Help** — Track orders, cancellations, returns\n" +
                       "💳 **Payment Info** — Payment methods, security\n" +
                       "🔄 **Return Policy** — Returns and refunds\n\n" +
                       "Just type your question!";
            }

            // Product recommendations
            if (matchesAny(msg, "recommend", "suggest", "show me", "popular", "best seller", "trending", "what should i buy", "gift")) {
                List<Product> products = productRepository.findAll();
                if (products.isEmpty()) {
                    return "We're currently updating our catalog. Please check back soon! 🎨";
                }
                
                // Pick up to 5 random products
                Collections.shuffle(products);
                List<Product> recommended = products.stream().limit(5).collect(Collectors.toList());
                
                StringBuilder sb = new StringBuilder("🌟 **Here are some handpicked products for you:**\n\n");
                for (Product p : recommended) {
                    sb.append("• **").append(p.getName()).append("** — ₹").append(p.getPrice());
                    if (p.getCategory() != null) sb.append(" (").append(p.getCategory()).append(")");
                    sb.append("\n");
                }
                sb.append("\n💡 Browse our full collection at the Products page for more!");
                return sb.toString();
            }

            // Category-specific search
            if (matchesAny(msg, "pottery", "ceramic", "vase", "clay")) {
                return searchByCategory("Pottery");
            }
            if (matchesAny(msg, "textile", "saree", "shawl", "kurti", "dupatta", "fabric", "silk", "cotton", "embroidery", "chikankari", "phulkari", "pashmina")) {
                return searchByCategory("Textiles");
            }
            if (matchesAny(msg, "wood", "wooden", "carving", "sandalwood", "toy")) {
                return searchByCategory("Wood Craft");
            }
            if (matchesAny(msg, "metal", "brass", "copper", "iron", "bidri", "dhokra")) {
                return searchByCategory("Metal Craft");
            }
            if (matchesAny(msg, "paint", "art", "canvas", "madhubani", "warli", "tribal")) {
                return searchByCategory("Paintings");
            }
            if (matchesAny(msg, "home decor", "decor", "jute", "bamboo", "basket", "macrame")) {
                return searchByCategory("Home Decor");
            }

            // Price-related queries
            if (matchesAny(msg, "cheap", "affordable", "budget", "under 1000", "low price", "inexpensive")) {
                List<Product> affordable = productRepository.findAll().stream()
                        .filter(p -> p.getPrice().doubleValue() <= 1000)
                        .limit(5)
                        .collect(Collectors.toList());
                if (affordable.isEmpty()) {
                    return "We have a wide range of products! Check the Products page and sort by 'Price: Low to High' to find the best deals. 💰";
                }
                StringBuilder sb = new StringBuilder("💰 **Budget-friendly picks (under ₹1000):**\n\n");
                for (Product p : affordable) {
                    sb.append("• **").append(p.getName()).append("** — ₹").append(p.getPrice()).append("\n");
                }
                return sb.toString();
            }

            if (matchesAny(msg, "expensive", "premium", "luxury", "high end", "exclusive")) {
                List<Product> premium = productRepository.findAll().stream()
                        .sorted(Comparator.comparing(Product::getPrice).reversed())
                        .limit(5)
                        .collect(Collectors.toList());
                if (premium.isEmpty()) {
                    return "Our premium collection is being curated. Stay tuned! ✨";
                }
                StringBuilder sb = new StringBuilder("✨ **Premium handcrafted pieces:**\n\n");
                for (Product p : premium) {
                    sb.append("• **").append(p.getName()).append("** — ₹").append(p.getPrice()).append("\n");
                }
                return sb.toString();
            }

            // Order tracking
            if (matchesAny(msg, "track", "order status", "where is my order", "order track", "delivery status", "when will", "shipping")) {
                return "📦 **Tracking Your Order:**\n\n" +
                       "1. Go to **My Orders** from the menu\n" +
                       "2. Find your order and check the current status\n" +
                       "3. Status flow: Pending → Processing → Shipped → Delivered\n\n" +
                       "⏱️ Typical delivery: **5-7 business days** after order placement.\n" +
                       "📧 You'll receive notifications at each stage!";
            }

            // Order cancellation
            if (matchesAny(msg, "cancel", "cancellation", "cancel order", "want to cancel")) {
                return "❌ **Order Cancellation Policy:**\n\n" +
                       "• You can cancel within **24 hours** of placing the order\n" +
                       "• Go to **My Orders** → Click on the order → **Cancel Order**\n" +
                       "• Provide a reason for cancellation\n" +
                       "• Refund will be processed within 5-7 business days\n\n" +
                       "⚠️ Orders that have been shipped cannot be cancelled. Please use the return option instead.";
            }

            // Return policy
            if (matchesAny(msg, "return", "refund", "exchange", "money back", "damaged", "defective", "wrong item")) {
                return "🔄 **Return & Refund Policy:**\n\n" +
                       "• **7-day return window** from delivery date\n" +
                       "• Items must be unused and in original packaging\n" +
                       "• Handcrafted items with minor variations are not eligible for return (artisan uniqueness)\n" +
                       "• **Damaged/defective items**: Full refund + free return shipping\n" +
                       "• **Wrong item**: Free replacement + return shipping\n" +
                       "• Refund processed within **5-7 business days** after we receive the return\n\n" +
                       "📧 Contact support through the Messages section for return assistance.";
            }

            // Payment info
            if (matchesAny(msg, "payment", "pay", "upi", "card", "credit card", "debit card", "netbanking", "cod", "cash on delivery")) {
                return "💳 **Payment Methods We Accept:**\n\n" +
                       "• 💳 Credit/Debit Cards (Visa, Mastercard, RuPay)\n" +
                       "• 📱 UPI (Google Pay, PhonePe, Paytm)\n" +
                       "• 🏦 Net Banking (All major banks)\n" +
                       "• 💵 Cash on Delivery (COD)\n\n" +
                       "🔒 All payments are **100% secure** with SSL encryption.\n" +
                       "📃 You'll receive a payment confirmation email after checkout.";
            }

            // Artisan info
            if (matchesAny(msg, "artisan", "maker", "craftsman", "who made", "handmade", "seller")) {
                return "🧑‍🎨 **About Our Artisans:**\n\n" +
                       "Every product on HandCraft is made by skilled Indian artisans preserving traditional craftsmanship.\n\n" +
                       "• Each product page shows the **artisan's story**\n" +
                       "• You can **message artisans directly** from product pages\n" +
                       "• Our artisans specialize in: pottery, textiles, wood carving, metal work, paintings & more\n\n" +
                       "🤝 By purchasing, you directly support their livelihood and help preserve cultural heritage!";
            }

            // Account/Profile
            if (matchesAny(msg, "account", "profile", "change name", "change password", "update profile", "settings")) {
                return "👤 **Account Settings:**\n\n" +
                       "• Go to **Profile** from the top-right menu\n" +
                       "• **Edit Profile** tab: Update your name and phone number\n" +
                       "• **Change Password** tab: Update your password securely\n\n" +
                       "💡 Your email address cannot be changed as it's your login identifier.";
            }

            // Wishlist
            if (matchesAny(msg, "wishlist", "save for later", "favorites", "liked items")) {
                return "❤️ **Wishlist Feature:**\n\n" +
                       "• Click the **heart icon** on any product to add it to your wishlist\n" +
                       "• View your wishlist from the **heart icon** in the navbar\n" +
                       "• Easily **move items to cart** when you're ready to buy\n" +
                       "• Your wishlist is saved and synced across devices!";
            }

            // Cart
            if (matchesAny(msg, "cart", "shopping bag", "add to cart", "checkout")) {
                return "🛒 **Shopping Cart:**\n\n" +
                       "• Click **Add to Cart** on any product page\n" +
                       "• Adjust quantities in your cart\n" +
                       "• Apply coupon codes at checkout for discounts!\n" +
                       "• Available coupons: **WELCOME10** (10% off) and **SAVER500** (₹500 off on orders above ₹3000)";
            }

            // Coupon/Discount
            if (matchesAny(msg, "coupon", "discount", "promo", "offer", "deal", "code")) {
                return "🏷️ **Available Coupons:**\n\n" +
                       "• **WELCOME10** — 10% off on orders above ₹500\n" +
                       "• **SAVER500** — Flat ₹500 off on orders above ₹3000\n\n" +
                       "💡 Apply coupon codes during checkout! Keep an eye on our page for seasonal offers.";
            }

            // Help
            if (matchesAny(msg, "help", "support", "assist", "what can you do", "options", "menu")) {
                return "🤖 **I can help you with:**\n\n" +
                       "🛍️ Product recommendations — \"Show me textiles\" or \"Recommend products\"\n" +
                       "📦 Order help — \"Track my order\" or \"Cancel order\"\n" +
                       "💳 Payment — \"Payment methods\" or \"UPI payment\"\n" +
                       "🔄 Returns — \"Return policy\" or \"Refund\"\n" +
                       "🏷️ Coupons — \"Available coupons\" or \"Discount codes\"\n" +
                       "❤️ Wishlist — \"How to save items\"\n" +
                       "👤 Account — \"Update profile\" or \"Change password\"\n" +
                       "🧑‍🎨 Artisans — \"Tell me about artisans\"\n\n" +
                       "Just type naturally and I'll do my best to help! 😊";
            }

            // Thank you
            if (matchesAny(msg, "thank", "thanks", "thx", "appreciate")) {
                return "You're welcome! 😊 Happy to help. Is there anything else you'd like to know about HandCraft?";
            }

            // Bye
            if (matchesAny(msg, "bye", "goodbye", "see you", "later", "quit", "exit")) {
                return "Goodbye! 👋 Happy shopping at HandCraft! Come back anytime you need help. 🎨";
            }

            // Try product name search as a last resort
            List<Product> matchedProducts = productRepository.findAll().stream()
                    .filter(p -> p.getName().toLowerCase().contains(msg) || 
                                 (p.getDescription() != null && p.getDescription().toLowerCase().contains(msg)) ||
                                 msg.contains(p.getName().toLowerCase()))
                    .limit(3)
                    .collect(Collectors.toList());

            if (!matchedProducts.isEmpty()) {
                StringBuilder sb = new StringBuilder("🔍 **Found these products matching your query:**\n\n");
                for (Product p : matchedProducts) {
                    sb.append("• **").append(p.getName()).append("** — ₹").append(p.getPrice());
                    sb.append(" | ").append(p.getStock() > 0 ? "In Stock ✅" : "Out of Stock ❌").append("\n");
                    if (p.getDescription() != null) {
                        sb.append("  _").append(p.getDescription().length() > 80 ? p.getDescription().substring(0, 80) + "..." : p.getDescription()).append("_\n");
                    }
                }
                sb.append("\nVisit the product page for full details!");
                return sb.toString();
            }

            // Default fallback
            return "I'm not sure I understand that. 🤔 Here are things I can help with:\n\n" +
                   "• **\"Recommend products\"** — Get personalized suggestions\n" +
                   "• **\"Track my order\"** — Order status help\n" +
                   "• **\"Payment methods\"** — How to pay\n" +
                   "• **\"Return policy\"** — Returns and refunds\n" +
                   "• **\"Help\"** — See all available topics\n\n" +
                   "Try typing one of these or ask a specific question!";
        } catch (Exception e) {
            return "I encountered an issue processing your request. Could you please rephrase your question? 🙏";
        }
    }

    private String searchByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);
        if (products.isEmpty()) {
            return "We don't have " + category + " products right now, but check back soon! 🎨\n\nWant me to suggest something else?";
        }
        StringBuilder sb = new StringBuilder("🎨 **" + category + " Collection:**\n\n");
        for (Product p : products.stream().limit(5).collect(Collectors.toList())) {
            sb.append("• **").append(p.getName()).append("** — ₹").append(p.getPrice());
            sb.append(" | By ").append(p.getArtisan() != null ? p.getArtisan().getName() : "Unknown");
            sb.append(p.getStock() > 0 ? " ✅" : " (Out of stock)").append("\n");
        }
        if (products.size() > 5) {
            sb.append("\n...and ").append(products.size() - 5).append(" more! Browse the full collection on the Products page.");
        }
        return sb.toString();
    }

    private boolean matchesAny(String input, String... keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword)) return true;
        }
        return false;
    }
}
