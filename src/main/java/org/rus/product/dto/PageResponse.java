package org.rus.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response with pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

    @Schema(description = "Page content")
    private List<T> content;

    @Schema(description = "Current page number", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of elements", example = "1543")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "78")
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Whether the content is empty", example = "false")
    private boolean empty;

    public static <T> PageResponse<T> of(List<T> content, PageRequest pageRequest, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.getSize());

        return PageResponse.<T>builder()
                .content(content)
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageRequest.getPage() == 0)
                .last(pageRequest.getPage() >= totalPages - 1)
                .empty(content.isEmpty())
                .build();
    }

}