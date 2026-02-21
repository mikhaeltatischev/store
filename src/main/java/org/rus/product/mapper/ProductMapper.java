package org.rus.product.mapper;

import org.rus.product.domain.Money;
import org.rus.product.domain.Product;
import org.rus.product.dto.ProductResponse;
import org.rus.product.dto.ProductSummaryResponse;
import org.rus.product.infrastructure.model.ProductEntity;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

public class ProductMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());

    public static ProductResponse toResponse(Product product) {
        if (product == null) return null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .keywords(product.getKeywords())
                .price(product.getPrice() != null ? product.getPrice().getAmount() : null)
                .currency(product.getPrice() != null ?
                        product.getPrice().getCurrency().getCurrencyCode() : null)
                .finalPrice(product.getFinalPrice() != null ?
                        product.getFinalPrice().getAmount() : null)
                .count(product.getCount())
                .discount(product.getDiscount())
                .categoryId(product.getCategoryId())
                .creatorId(product.getCreatorId())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .available(product.isAvailable())
                .createdAt(product.getCreatedAt().format(DATE_FORMATTER))
                .lastModifiedAt(product.getLastModifiedAt().format(DATE_FORMATTER))
                .build();
    }

    public static ProductSummaryResponse toSummaryResponse(Product product) {
        if (product == null) return null;

        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .finalPrice(product.getFinalPrice() != null ?
                        product.getFinalPrice().getAmount() : null)
                .currency(product.getPrice() != null ?
                        product.getPrice().getCurrency().getCurrencyCode() : null)
                .available(product.isAvailable())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .build();
    }

    /**
     * Convert from domain model to JPA entity
     */
    public static ProductEntity fromDomain(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setBrand(product.getBrand());
        entity.setShortDescription(product.getShortDescription());
        entity.setDescription(product.getDescription());
        entity.setKeywords(product.getKeywords());
        if (product.getPrice() != null) {
            entity.setPriceAmount(product.getPrice().getAmount());
            entity.setPriceCurrency(product.getPrice().getCurrency().getCurrencyCode());
        }
        entity.setCount(product.getCount());
        entity.setDiscount(product.getDiscount());
        entity.setCategoryId(product.getCategoryId());
        entity.setCreatorId(product.getCreatorId());
        entity.setStatus(product.getStatus());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setLastModifiedAt(product.getLastModifiedAt());
        entity.setDeletedAt(product.getDeletedAt());
        return entity;
    }

    /**
     * Convert from JPA entity to domain model
     */
    public static Product toDomain(ProductEntity productEntity) {
        return Product.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .brand(productEntity.getBrand())
                .shortDescription(productEntity.getShortDescription())
                .description(productEntity.getDescription())
                .keywords(productEntity.getKeywords())
                .price(Money.of(productEntity.getPriceAmount(), Currency.getInstance(productEntity.getPriceCurrency())))
                .count(productEntity.getCount())
                .discount(productEntity.getDiscount())
                .categoryId(productEntity.getCategoryId())
                .creatorId(productEntity.getCreatorId())
                .status(productEntity.getStatus())
                .createdAt(productEntity.getCreatedAt())
                .lastModifiedAt(productEntity.getLastModifiedAt())
                .deletedAt(productEntity.getDeletedAt())
                .build();
    }

}