package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByArtisanId(Long artisanId);
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String keyword);
}
