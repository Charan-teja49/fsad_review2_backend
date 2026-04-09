package com.ecommerce.backend.dto;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class OrderRequest {
    @JsonProperty("address_id")
    private Long addressId;
    
    @JsonProperty("payment_method")
    private String paymentMethod;
    
    @JsonProperty("coupon_code")
    private String couponCode;
}
