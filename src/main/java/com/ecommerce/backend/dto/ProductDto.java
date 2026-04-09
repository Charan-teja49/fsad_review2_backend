package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal compare_price;
    private Integer stock;
    private String category;
    private String category_name;
    private String image_url;
    private String imageUrls;
    private Long artisan_id;
    private String artisan_name;
    private Double avg_rating;
    private Integer review_count;
    private String createdAt;
}
