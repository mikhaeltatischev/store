package org.rus.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Pagination request parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination request parameters")
public class PageRequest {

    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    @Builder.Default
    private int page = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    @Builder.Default
    private int size = 20;

}