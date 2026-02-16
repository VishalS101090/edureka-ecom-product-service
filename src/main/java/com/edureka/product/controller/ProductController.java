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
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found for id: " + id)));
    }

    /**
     * Get product by SKU code. Used by Order/Inventory services for inter-service communication.
     */
    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<?> getProductBySkuCode(@PathVariable String skuCode) {
        if (skuCode == null || skuCode.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Product skuCode is required"));
        }
        _logger.info("Getting product with skuCode: {}", skuCode);
        return productRepository.findBySkuCode(skuCode)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found for skuCode: " + skuCode)));
    }
}
