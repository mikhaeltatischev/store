package org.rus.product.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.rus.product.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product domain model.
 * Contains only business logic and product attributes.
 * Has no connection to orders, categories etc. - only an ID.
 */
@Data
@Builder
@Slf4j
public class Product {

    private final UUID id;
    private String name;
    private String article;
    private String brand;
    private String shortDescription;
    private String description;
    private String keywords;
    private Money price;
    private Integer count;
    private Double discount;
    private UUID categoryId;
    private UUID creatorId;
    private Status status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime deletedAt;

    /**
     * Mark product as deleted (soft delete)
     */
    public void markAsDeleted() {
        log.trace("Set product with id: {} to deleted", id);
        this.status = Status.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Verifying, Can the user authorize the product
     */
    public boolean checkCreator(UUID userId) {
        boolean isCreator = creatorId.equals(userId);
        log.debug("Product {} creator check for user {}: {}", id, userId, isCreator);
        return !isCreator;
    }

    /**
     * Decrease product quantity (when create order)
     */
    public void decreaseStock(int quantity) {
        log.info("Decreasing stock for product {} by {}", id, quantity);

        if (quantity <= 0) {
            log.warn("Invalid quantity {} for decreasing stock of product {}", quantity, id);
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (count < quantity) {
            log.warn("Insufficient stock for product {}. Requested: {}, available: {}",
                    id, quantity, count);
            throw new IllegalStateException("Not enough items in stock");
        }

        this.count -= quantity;
        this.lastModifiedAt = LocalDateTime.now();
        log.debug("Product {} new stock count: {}", id, count);

        if (count == 0) {
            this.status = Status.OUT_OF_STOCK;
            log.trace("Product {} is now OUT_OF_STOCK", id);
        }
    }

    /**
     * Increase product quantity (when returning or popup)
     */
    public void increaseStock(int quantity) {
        log.trace("Increasing stock for product {} by {}", id, quantity);

        if (quantity <= 0) {
            log.warn("Invalid quantity {} for increasing stock of product {}", quantity, id);
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.count += quantity;
        this.lastModifiedAt = LocalDateTime.now();
        log.trace("Product {} new stock count: {}", id, count);

        if (status == Status.OUT_OF_STOCK && count > 0) {
            this.status = Status.ACTIVE;
            log.trace("Product {} is now ACTIVE again", id);
        }
    }

    /**
     * Update product details
     * */
    public void updateDetails(String name, String brand, String shortDescription,
                              String description, String keywords, Double discount,
                              UUID categoryId, UUID editorId, Double price, Integer count) {
        log.info("Updating details for product {} by user {}", id, editorId);

        /*if (checkCreator(editorId)) {
            log.warn("User {} attempted to update details of product {} but is not the creator",
                    editorId, id);
            throw new SecurityException("Only creator can update product details");
        }*/

        this.price = price != null ? Money.rub(price) : this.price;
        this.name = name != null ? name : this.name;
        this.brand = brand != null ? brand : this.brand;
        this.shortDescription = shortDescription != null ? shortDescription : this.shortDescription;
        this.description = description != null ? description : this.description;
        this.keywords = keywords != null ? keywords : this.keywords;
        this.count = count != null ? count : this.count;

        if (discount != null) {
            if (discount < 0 || discount > 100) {
                log.error("Invalid discount value {} for product {}", discount, id);
                throw new IllegalArgumentException("Discount must be between 0 and 100");
            }
            this.discount = discount;
        }

        this.categoryId = categoryId != null ? categoryId : this.categoryId;
        this.lastModifiedAt = LocalDateTime.now();

        log.debug("Product {} details updated successfully", id);
    }

    /**
     * Calculate final price
     * */
    public Money getFinalPrice() {
        if (discount == null || discount == 0) {
            log.debug("Product {} final price (no discount): {}", id, price);
            return price;
        }
        BigDecimal discountMultiplier = BigDecimal.valueOf(1 - discount / 100);
        Money finalPrice = price.multiply(discountMultiplier);
        log.debug("Product {} final price with discount {}%: {} -> {}",
                id, discount, price, finalPrice);
        return finalPrice;
    }

    /**
     * Check status on available
     * */
    public boolean isAvailable() {
        boolean available = status == Status.ACTIVE && count != null && count > 0;
        log.debug("Product {} availability check: {}", id, available);
        return available;
    }

}