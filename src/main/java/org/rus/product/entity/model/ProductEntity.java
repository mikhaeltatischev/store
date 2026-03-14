package org.rus.product.entity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.*;
import org.rus.product.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@SQLDelete(sql = "UPDATE products SET deleted_at = now() WHERE product_id = ?")
@SQLRestriction("status <> 'DELETED'")
@Data
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String brand;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(length = 2000)
    private String description;

    private String keywords;  // example: Tool;hammer...etc..

    @Column(nullable = false, precision = 10, scale = 2, name = "price_amount")
    private BigDecimal priceAmount;

    @Column(nullable = false, length = 3, name = "price_currency")
    private String priceCurrency;  // ISO currency code (RUB, USD, EUR)

    private Integer count;

    private Double discount;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }

}