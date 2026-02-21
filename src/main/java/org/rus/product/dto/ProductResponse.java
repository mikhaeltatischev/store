package org.rus.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for product response with full information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full product information response")
public class ProductResponse {

    @Schema(description = "Unique product identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Product name", example = "Smartphone XYZ Pro")
    private String name;

    @Schema(description = "Product brand", example = "Samsung")
    private String brand;

    @Schema(description = "Short description", example = "Flagship smartphone with excellent camera")
    private String shortDescription;

    @Schema(description = "Full description", example = "Detailed description of features and specifications...")
    private String description;

    @Schema(description = "Search keywords", example = "smartphone, phone, android, samsung")
    private String keywords;

    @Schema(description = "Product price", example = "29999.99")
    private BigDecimal price;

    @Schema(description = "Currency", example = "RUB")
    private String currency;

    @Schema(description = "Final price with discount", example = "28349.99")
    private BigDecimal finalPrice;

    @Schema(description = "Stock quantity", example = "10")
    private Integer count;

    @Schema(description = "Discount percentage", example = "5.5")
    private Double discount;

    @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID categoryId;

    @Schema(description = "Creator ID (seller)",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID creatorId;

    @Schema(description = "Product status",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "OUT_OF_STOCK", "DISCONTINUED", "DELETED"})
    private String status;

    @Schema(description = "Whether the product is available for order", example = "true")
    private boolean available;

    @Schema(description = "Creation timestamp", example = "2023-12-05T10:30:00Z")
    private String createdAt;

    @Schema(description = "Last modification timestamp", example = "2023-12-06T15:45:00Z")
    private String lastModifiedAt;

}