package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AuthRequest;
import com.ecommerce.backend.dto.AuthResponse;
import com.ecommerce.backend.dto.RegisterRequest;
import com.ecommerce.backend.dto.UserDto;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.service.AuthService;
import com.ecommerce.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    // Frontend posts to /auth/signup
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Keep /register as alias
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Returns user profile without exposing password
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        User user = userService.getUserProfile(authentication.getName());
        UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail(), 
                user.getRole().name().toLowerCase(), user.getPhone());
        return ResponseEntity.ok(dto);
    }

    // UPDATE profile (name, phone)
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(@RequestBody Map<String, String> body, Authentication authentication) {
        String name = body.get("name");
        String phone = body.get("phone");
        User updated = userService.updateUserProfile(authentication.getName(), name, phone);
        UserDto dto = new UserDto(updated.getId(), updated.getName(), updated.getEmail(),
                updated.getRole().name().toLowerCase(), updated.getPhone());
        return ResponseEntity.ok(dto);
    }

    // CHANGE password
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> body, Authentication authentication) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Both current and new password are required"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters"));
        }
        
        userService.changePassword(authentication.getName(), currentPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // Refresh token endpoint (frontend calls this on 401)
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        // For now, return a simple error since we use same token for access+refresh
        return ResponseEntity.badRequest().body(Map.of("message", "Token refresh not supported, please login again"));
    }
}
