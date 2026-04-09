package com.ecommerce.backend.dto;
import lombok.Data;
import lombok.AllArgsConstructor;
@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;
    private String message;
}
