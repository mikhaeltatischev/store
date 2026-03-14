package org.rus.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.rus.product.domain.Product;
import org.rus.product.dto.*;
import org.rus.product.mapper.ProductMapper;
import org.rus.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@Slf4j
@Tag(name = "Products", description = "API for product management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product and returns its full information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product successfully created",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody
            @Parameter(description = "Product creation data", required = true)
            CreateProductRequest request) {

        log.info("REST request to create product: {}", request.getName());

        // Temporary solution - will be replaced with AuthPort
        UUID mockCreatorId = UUID.randomUUID();

        Product product = productService.createProduct(
                request.getName(),
                request.getBrand(),
                request.getShortDescription(),
                request.getDescription(),
                request.getKeywords(),
                request.getPrice(),
                request.getCount(),
                request.getDiscount(),
                request.getCategoryId(),
                mockCreatorId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductMapper.toResponse(product));
    }

    @GetMapping
    @Operation(summary = "Get product by ID", description = "Returns full product information by its identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Product ID is required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @RequestParam(name = "id") UUID id) {

        log.info("REST request to get product by id: {}", id);

        Product product = productService.getProduct(id);

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @PutMapping
    @Operation(summary = "Update product details", description = "Updates product information and returns updated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product successfully updated",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or product ID"),
            @ApiResponse(responseCode = "403", description = "No permission to update"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @RequestParam(name = "id") UUID id,

            @Valid @RequestBody
            @Parameter(description = "Update data", required = true)
            UpdateProductRequest request) {

        log.info("REST request to update product: {}", id);

        // Temporary solution - will be replaced with AuthPort
        UUID mockEditorId = UUID.randomUUID();

        Product product = productService.updateProductDetails(
                id,
                request.getName(),
                request.getBrand(),
                request.getShortDescription(),
                request.getDescription(),
                request.getKeywords(),
                request.getDiscount(),
                request.getCategoryId(),
                mockEditorId,
                request.getPrice(),
                request.getCount()
        );

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @PostMapping("/decrease-stock")
    @Operation(summary = "Decrease product stock", description = "Decreases product stock quantity (when sold)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock successfully decreased",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> decreaseStock(
            @Parameter(description = "Product ID", required = true)
            @RequestParam(name = "id") UUID id,

            @Parameter(description = "Quantity to decrease", example = "1", required = true)
            @RequestParam(name = "quantity") int quantity) {

        log.info("REST request to decrease stock for product {} by {}", id, quantity);

        Product product = productService.decreaseStock(id, quantity);

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @PostMapping("/increase-stock")
    @Operation(summary = "Increase product stock", description = "Increases product stock quantity (when restocking)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock successfully increased",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> increaseStock(
            @Parameter(description = "Product ID", required = true)
            @RequestParam(name = "id") UUID id,

            @Parameter(description = "Quantity to increase", example = "5", required = true)
            @RequestParam(name = "quantity") int quantity) {

        log.info("REST request to increase stock for product {} by {}", id, quantity);

        Product product = productService.increaseStock(id, quantity);

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available products", description = "Returns paginated list of products in stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products successfully retrieved",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getAvailableProducts(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0", name = "page") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20", name = "size") int size) {

        log.info("REST request to get available products, page: {}, size: {}", page, size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page < 0 ? 0 : page)
                .size(size)
                .build();

        PageResponse<Product> products = productService.getAvailableProducts(pageRequest);

        PageResponse<ProductSummaryResponse> response = PageResponse.of(
                products.getContent().stream()
                        .map(ProductMapper::toSummaryResponse)
                        .toList(),
                pageRequest,
                products.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "Delete product", description = "Soft delete of product (marked as DELETED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Product ID is required"),
            @ApiResponse(responseCode = "403", description = "No permission to delete"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true)
            @RequestParam(name = "id") UUID id) {

        log.info("REST request to delete product: {}", id);

        // Temporary solution - will be replaced with AuthPort
        UUID mockEditorId = UUID.randomUUID();

        productService.deleteProduct(id, mockEditorId);

        return ResponseEntity.noContent().build();
    }

}