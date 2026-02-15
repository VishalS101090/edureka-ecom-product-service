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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    public static final Logger _logger= LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("addproduct")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        if (product == null) {
            _logger.warn("product is null");
            return ResponseEntity.badRequest().body("Product is null");
        }
        if(product.getName()==null || product.getName().isBlank())
        {
            _logger.warn("product name is null");
            return ResponseEntity.badRequest().body("product name is null");
        }


        product.setId(UUID.randomUUID().toString());
        Product prod =productRepository.save(product);
        _logger.info("New product Added Successfully:"+prod.toString());
        return ResponseEntity.ok("New product Added Successfully id: "+prod.getId());
    }

    @GetMapping("all")
    public List<Product> getAllProducts() {
        _logger.info("Getting all products");
        return productRepository.findAll();
    }

    @GetMapping("product")
    public Optional<Product> getProduct(@RequestParam String id) {
        _logger.info("Getting product with id: "+id);
        return productRepository.findById(id);
    }
}
