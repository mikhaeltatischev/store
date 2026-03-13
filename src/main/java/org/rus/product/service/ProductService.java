package org.rus.product.service;

import lombok.extern.slf4j.Slf4j;
import org.rus.product.domain.Money;
import org.rus.product.domain.Product;
import org.rus.product.dto.PageRequest;
import org.rus.product.dto.PageResponse;
import org.rus.product.enums.Status;
import org.rus.product.exception.ProductNotFoundException;
import org.rus.product.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product management service.
 * Contains business logic related to products.
 */
@Service
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        log.info("ProductService initialized");
    }

    /**
     * Create new product
     */
    public Product createProduct(String name, String brand,
                                 String shortDescription, String description,
                                 String keywords, double price, Integer count,
                                 Double discount, UUID categoryId, UUID creatorId) {

        log.trace("Creating new product: name='{}', creatorId={}",
                name, creatorId);

        // Create domain model
        Product product = Product.builder()
                .name(name)
                .status(Status.CREATED)
                .createdAt(LocalDateTime.now())
                .brand(brand)
                .shortDescription(shortDescription)
                .description(description)
                .keywords(keywords)
                .price(Money.rub(price))
                .count(count)
                .discount(discount == null ? 0.0 : discount)
                .categoryId(categoryId)
                .creatorId(creatorId)
                .build();

        log.debug("Product domain model created: {}", product.getId());

        // Save
        Product saved = productRepository.save(product);

        log.trace("Product created successfully: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    /**
     * Get product by ID
     */
    public Product getProduct(UUID productId) {
        log.trace("Fetching product by id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found: {}", productId);
                    return new ProductNotFoundException(productId);
                });

        log.trace("Product found: {} ({})", product.getName(), productId);
        return product;
    }

    /**
     * Update product details
     */
    public Product updateProductDetails(UUID productId, String name, String brand,
                                        String shortDescription, String description,
                                        String keywords, Double discount,
                                        UUID categoryId, UUID editorId, Double price, Integer count) {
        log.trace("Updating details for product {} by editor {}", productId, editorId);

        Product product = getProduct(productId);

        product.updateDetails(name, brand, shortDescription, description,
                keywords, discount, categoryId, editorId, price, count);

        Product updated = productRepository.save(product);

        log.trace("Product details updated successfully: {}", productId);
        return updated;
    }

    /**
     * Decrease stock (upon sale)
     */
    public Product decreaseStock(UUID productId, int quantity) {
        log.trace("Decreasing stock for product {} by {}", productId, quantity);

        Product product = getProduct(productId);

        product.decreaseStock(quantity);

        Product updated = productRepository.save(product);

        log.trace("Stock decreased successfully for product {}. New count: {}",
                productId, updated.getCount());
        return updated;
    }

    /**
     * Increase product count (upon replenishment)
     */
    public Product increaseStock(UUID productId, int quantity) {
        log.trace("Increasing stock for product {} by {}", productId, quantity);

        Product product = getProduct(productId);

        product.increaseStock(quantity);

        Product updated = productRepository.save(product);

        log.trace("Stock increased successfully for product {}. New count: {}",
                productId, updated.getCount());
        return updated;
    }

    /**
     * Get available products (count greater than 0)
     */
    public PageResponse<Product> getAvailableProducts(PageRequest pageRequest) {
        log.trace("Fetching available products, page: {}, size: {}",
                pageRequest.getPage(), pageRequest.getSize());

        PageResponse<Product> result = productRepository.findAvailableProducts(pageRequest);

        log.trace("Found {} available products (total: {})",
                result.getContent().size(), result.getTotalElements());

        return result;
    }

    /**
     * Soft delete
     */
    public void deleteProduct(UUID productId, UUID editorId) {
        log.trace("Deleting product {} by editor {}", productId, editorId);

        Product product = getProduct(productId);

        productRepository.delete(product);

        log.trace("Product deleted successfully: {}", productId);
    }

}