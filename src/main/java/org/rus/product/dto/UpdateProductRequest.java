package org.rus.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating an existing product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing product")
public class UpdateProductRequest {

    @Schema(description = "Product name", example = "Smartphone XYZ Pro Max")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @Schema(description = "Product brand", example = "Samsung")
    private String brand;

    @Schema(description = "Short description", example = "New flagship with improved camera")
    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;

    @Schema(description = "Full description", example = "Updated detailed description...")
    @Size(max = 2000, message = "Full description cannot exceed 2000 characters")
    private String description;

    @Schema(description = "Search keywords", example = "smartphone, phone, android, samsung, flagship")
    private String keywords;

    @Schema(description = "Product price in rubles", example = "27999.99")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Price cannot exceed 9,999,999.99")
    private Double price;

    @Schema(description = "Stock quantity", example = "15")
    @Min(value = 0, message = "Count cannot be negative")
    @Max(value = 999999, message = "Count cannot exceed 999,999")
    private Integer count;

    @Schema(description = "Discount percentage (0-100)", example = "10.0")
    @DecimalMin(value = "0", message = "Discount cannot be negative")
    @DecimalMax(value = "100", message = "Discount cannot exceed 100%")
    private Double discount;

    @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID categoryId;

}