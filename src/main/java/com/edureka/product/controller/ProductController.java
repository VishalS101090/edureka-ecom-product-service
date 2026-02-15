package com.edureka.product.controller;

import com.edureka.product.model.Product;
import com.edureka.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    public static final Logger _logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    /**
     * Create a new product.
     * Returns 201 Created with the created product, or 400 for validation errors.
     */
    @PostMapping(value = {"", "/", "/addproduct"})
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        if (product == null) {
            _logger.warn("Product is null");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product body is required"));
        }
        if (product.getName() == null || product.getName().isBlank()) {
            _logger.warn("Product name is null or blank");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product name is required"));
        }
        if (product.getPrice() != null && product.getPrice().signum() < 0) {
            _logger.warn("Product price is negative");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product price must be non-negative"));
        }

        product.setId(UUID.randomUUID().toString());
        Product saved = productRepository.save(product);
        _logger.info("New product added successfully: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get all products. Optional filter by category.
     * Returns 200 OK with list (empty list if none or no match).
     */
    @GetMapping(value = {"", "/", "/all"})
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) String category) {
        _logger.info("Getting all products" + (category != null ? " for category: " + category : ""));
        List<Product> products = category != null && !category.isBlank()
                ? productRepository.findByCategory(category)
                : productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID (path variable). Returns 200 with product or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product id is required"));
        }
        _logger.info("Getting product with id: {}", id);
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found for id: " + id)));
    }

    /**
     * Get product by ID (query param). Kept for backward compatibility.
     * Returns 200 with product or 404 Not Found.
     */
    @GetMapping("/product")
    public ResponseEntity<?> getProductByQuery(@RequestParam String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product id is required"));
        }
        _logger.info("Getting product with id: {}", id);
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found for id: " + id)));
    }

    /**
     * Update an existing product by ID.
     * Returns 200 OK with updated product, 404 if not found, 400 for validation errors.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product product) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product id is required"));
        }
        if (product == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product body is required"));
        }
        if (product.getName() != null && product.getName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product name cannot be blank"));
        }
        if (product.getPrice() != null && product.getPrice().signum() < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product price must be non-negative"));
        }

        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) {
            _logger.warn("Product not found for update: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found for id: " + id));
        }

        Product toUpdate = existing.get();
        if (product.getName() != null) toUpdate.setName(product.getName());
        if (product.getDescription() != null) toUpdate.setDescription(product.getDescription());
        if (product.getPrice() != null) toUpdate.setPrice(product.getPrice());
        if (product.getCategory() != null) toUpdate.setCategory(product.getCategory());
        if (product.getImageUrl() != null) toUpdate.setImageUrl(product.getImageUrl());

        Product updated = productRepository.save(toUpdate);
        _logger.info("Product updated successfully: {}", id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a product by ID.
     * Returns 204 No Content on success, 404 if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product id is required"));
        }
        if (!productRepository.existsById(id)) {
            _logger.warn("Product not found for delete: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Product not found for id: " + id));
        }
        productRepository.deleteById(id);
        _logger.info("Product deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }
}
