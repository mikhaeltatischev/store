package org.rus.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for product summary information (for lists)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product summary information (for lists)")
public class ProductSummaryResponse {

    @Schema(description = "Unique product identifier",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Product name", example = "Smartphone XYZ Pro")
    private String name;

    @Schema(description = "Product brand", example = "Samsung")
    private String brand;

    @Schema(description = "Final price with discount", example = "28349.99")
    private BigDecimal finalPrice;

    @Schema(description = "Currency", example = "RUB")
    private String currency;

    @Schema(description = "Whether the product is available for order", example = "true")
    private boolean available;

    @Schema(description = "Product status", example = "ACTIVE")
    private String status;

}