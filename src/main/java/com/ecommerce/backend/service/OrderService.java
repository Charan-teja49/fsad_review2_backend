package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderRequest;
import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final NotificationRepository notificationRepository;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository, 
                        UserRepository userRepository, AddressRepository addressRepository,
                        NotificationRepository notificationRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Order createOrder(String email, OrderRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Address address = addressRepository.findById(request.getAddressId()).orElseThrow(() -> new RuntimeException("Address not found"));
        
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal total = cartItems.stream()
            .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalAmount(total);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            return orderItem;
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteByUserId(user.getId());

        // Create notifications for artisans whose products were ordered
        try {
            Set<User> artisans = cartItems.stream()
                    .map(ci -> ci.getProduct().getArtisan())
                    .collect(Collectors.toSet());

            for (User artisan : artisans) {
                if (artisan != null) {
                    Notification notif = new Notification();
                    notif.setUser(artisan);
                    notif.setType("order");
                    notif.setTitle("New Order Received! 🎉");
                    notif.setMessage("You have a new order (ORD-" + savedOrder.getId() + ") from " + user.getName() + ". Check your orders page to fulfill it.");
                    notificationRepository.save(notif);
                }
            }

            // Also notify the customer
            Notification customerNotif = new Notification();
            customerNotif.setUser(user);
            customerNotif.setType("order");
            customerNotif.setTitle("Order Placed Successfully!");
            customerNotif.setMessage("Your order ORD-" + savedOrder.getId() + " has been placed. Estimated delivery in 5-7 business days.");
            notificationRepository.save(customerNotif);
        } catch (Exception e) {
            // Don't fail the order if notification creation fails
            System.err.println("Failed to create order notifications: " + e.getMessage());
        }

        return savedOrder;
    }

    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserId(user.getId());
    }

    @Transactional
    public Order cancelOrder(Long orderId, String email, String reason) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order findById not found"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        long hoursSince = ChronoUnit.HOURS.between(order.getCreatedAt(), LocalDateTime.now());
        if (hoursSince > 24) {
            throw new RuntimeException("Orders can only be cancelled within 24 hours of placement.");
        }

        order.setStatus("cancelled");
        order.setCancelReason(reason);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderAddress(Long orderId, Long newAddressId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        long hoursSince = ChronoUnit.HOURS.between(order.getCreatedAt(), LocalDateTime.now());
        if (hoursSince > 6) {
            throw new RuntimeException("Order address can only be changed within 6 hours of placement.");
        }

        Address newAddress = addressRepository.findById(newAddressId).orElseThrow(() -> new RuntimeException("Address not found"));
        order.setAddress(newAddress);
        return orderRepository.save(order);
    }
}
