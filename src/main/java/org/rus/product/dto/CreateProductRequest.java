package org.rus.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for create product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request on create new product")
public class CreateProductRequest {

    @Schema(description = "Product name", example = "Iphone X", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Name length must be from 3 to 255 symbols")
    private String name;

    @Schema(description = "Product brand", example = "Samsung")
    private String brand;

    @Schema(description = "Short description", example = "Smartphone with a great charger")
    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;

    @Schema(description = "Full description", example = "Detailed description of characteristics and functions...")
    @Size(max = 2000, message = "Full description cannot exceed 2000 characters")
    private String description;

    @Schema(description = "Search keywords", example = "smartphone, phone, android, samsung")
    private String keywords;

    @Schema(description = "Product price in rubles", example = "29999.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Price cannot be greater than 9 999 999.99")
    private Double price;

    @Schema(description = "Quantity in stock", example = "10")
    @Min(value = 0, message = "The quantity cannot be negative")
    @Max(value = 999999, message = "The quantity cannot exceed 999,999")
    private Integer count;

    @Schema(description = "Discount in percent (0-100)", example = "5.5")
    @DecimalMin(value = "0", message = "The discount cannot be negative.")
    @DecimalMax(value = "100", message = "The discount cannot exceed 100%")
    private Double discount;

    @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID categoryId;

}
