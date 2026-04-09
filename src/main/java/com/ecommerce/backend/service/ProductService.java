package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getProductsByArtisan(Long artisanId) {
        return productRepository.findByArtisanId(artisanId);
    }

    public List<Product> getProductsByArtisanEmail(String email) {
        User artisan = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Artisan not found"));
        return productRepository.findByArtisanId(artisan.getId());
    }

    public Product createProduct(Product product, String artisanEmail) {
        User artisan = userRepository.findByEmail(artisanEmail)
                .orElseThrow(() -> new RuntimeException("Artisan not found"));
        product.setArtisan(artisan);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct, String email) {
        Product existing = getProductById(id);
        if (updatedProduct.getName() != null) existing.setName(updatedProduct.getName());
        if (updatedProduct.getDescription() != null) existing.setDescription(updatedProduct.getDescription());
        if (updatedProduct.getPrice() != null) existing.setPrice(updatedProduct.getPrice());
        if (updatedProduct.getStock() != null) existing.setStock(updatedProduct.getStock());
        if (updatedProduct.getCategory() != null) existing.setCategory(updatedProduct.getCategory());
        if (updatedProduct.getImageUrls() != null) existing.setImageUrls(updatedProduct.getImageUrls());
        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
