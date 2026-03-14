package org.rus.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rus.product.domain.Product;
import org.rus.product.dto.CreateProductRequest;
import org.rus.product.dto.ProductResponse;
import org.rus.product.dto.UpdateProductRequest;
import org.rus.product.entity.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Product Controller Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProductResponse createTestProduct(CreateProductRequest request) throws Exception {
        String requestBody = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResponse.class
        );
    }

    @Nested
    @DisplayName("PUT /products - Update Product")
    class UpdateProductTests {

        private UUID existingProductId;
        private CreateProductRequest originalProductRequest;
        private ProductResponse originalProduct;
        private UUID newCategoryId;

        @BeforeEach
        void setUp() throws Exception {
            UUID categoryId = UUID.randomUUID();
            newCategoryId = UUID.randomUUID();

            originalProductRequest = CreateProductRequest.builder()
                    .name("Original Product Name")
                    .brand("Original Brand")
                    .shortDescription("Original short description")
                    .description("Original full description with more details about the product")
                    .keywords("original, keywords, test")
                    .price(199.99)
                    .count(15)
                    .discount(5.0)
                    .categoryId(categoryId)
                    .build();

            originalProduct = createTestProduct(originalProductRequest);
            existingProductId = originalProduct.getId();
        }

        @Test
        @DisplayName("Should successfully update all product fields")
        void shouldUpdateAllProductFieldsSuccessfully() throws Exception {
            // Given
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Product Name")
                    .brand("Updated Brand")
                    .shortDescription("Updated short description")
                    .description("Updated full description with new details")
                    .keywords("updated, keywords, test")
                    .price(299.99)
                    .count(25)
                    .discount(10.0)
                    .categoryId(newCategoryId)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(updateRequest.getName())))
                    .andExpect(jsonPath("$.brand", is(updateRequest.getBrand())))
                    .andExpect(jsonPath("$.shortDescription", is(updateRequest.getShortDescription())))
                    .andExpect(jsonPath("$.description", is(updateRequest.getDescription())))
                    .andExpect(jsonPath("$.keywords", is(updateRequest.getKeywords())))
                    .andExpect(jsonPath("$.price", is(updateRequest.getPrice())))
                    .andExpect(jsonPath("$.currency", is("RUB")))
                    .andExpect(jsonPath("$.finalPrice", is(269.99))) // 299.99 - 10% = 269.991 -> 269.99
                    .andExpect(jsonPath("$.count", is(updateRequest.getCount())))
                    .andExpect(jsonPath("$.discount", is(updateRequest.getDiscount())))
                    .andExpect(jsonPath("$.categoryId", is(updateRequest.getCategoryId().toString())))
                    .andExpect(jsonPath("$.creatorId", is(originalProduct.getCreatorId().toString())))
                    .andExpect(jsonPath("$.status", is("CREATED")))
                    .andExpect(jsonPath("$.available", is(false)))
                    .andExpect(jsonPath("$.createdAt", is(originalProduct.getCreatedAt())))
                    .andExpect(jsonPath("$.lastModifiedAt", not(originalProduct.getLastModifiedAt())))
                    .andReturn();

            // Verify in database
            Product updatedProduct = productRepository.findById(existingProductId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getName()).isEqualTo(updateRequest.getName());
            assertThat(updatedProduct.getBrand()).isEqualTo(updateRequest.getBrand());
            assertThat(updatedProduct.getShortDescription()).isEqualTo(updateRequest.getShortDescription());
            assertThat(updatedProduct.getDescription()).isEqualTo(updateRequest.getDescription());
            assertThat(updatedProduct.getKeywords()).isEqualTo(updateRequest.getKeywords());
            assertThat(updatedProduct.getPrice().getAmount()).isEqualTo(BigDecimal.valueOf(updateRequest.getPrice()));
            assertThat(updatedProduct.getCount()).isEqualTo(updateRequest.getCount());
            assertThat(updatedProduct.getDiscount()).isEqualTo(updateRequest.getDiscount());
            assertThat(updatedProduct.getCategoryId()).isEqualTo(updateRequest.getCategoryId());
        }

        @Test
        @DisplayName("Should successfully update only selected fields")
        void shouldUpdateOnlySelectedFields() throws Exception {
            // Given - Update only name and price
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Only Name Updated")
                    .price(399.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(updateRequest.getName())))
                    .andExpect(jsonPath("$.brand", is(originalProductRequest.getBrand()))) // Unchanged
                    .andExpect(jsonPath("$.shortDescription", is(originalProductRequest.getShortDescription()))) // Unchanged
                    .andExpect(jsonPath("$.description", is(originalProductRequest.getDescription()))) // Unchanged
                    .andExpect(jsonPath("$.keywords", is(originalProductRequest.getKeywords()))) // Unchanged
                    .andExpect(jsonPath("$.price", is(updateRequest.getPrice())))
                    .andExpect(jsonPath("$.count", is(originalProductRequest.getCount()))) // Unchanged
                    .andExpect(jsonPath("$.discount", is(originalProductRequest.getDiscount()))) // Unchanged
                    .andExpect(jsonPath("$.categoryId", is(originalProductRequest.getCategoryId().toString()))); // Unchanged

            // Verify in database
            Product updatedProduct = productRepository.findById(existingProductId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getName()).isEqualTo(updateRequest.getName());
            assertThat(updatedProduct.getPrice().getAmount()).isEqualTo(BigDecimal.valueOf(updateRequest.getPrice()));
            assertThat(updatedProduct.getBrand()).isEqualTo(originalProductRequest.getBrand()); // Should be unchanged
        }

        @Test
        @DisplayName("Should update product and recalculate final price correctly")
        void shouldRecalculateFinalPriceAfterUpdate() throws Exception {
            // Given - Update discount
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .discount(20.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.price", is(originalProductRequest.getPrice())))
                    .andExpect(jsonPath("$.discount", is(20.0)))
                    .andExpect(jsonPath("$.finalPrice", is(159.99))); // 199.99 - 20% = 159.992

            // Given - Update price
            updateRequest = UpdateProductRequest.builder()
                    .price(500.00)
                    .build();

            requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.price", is(500.00)))
                    .andExpect(jsonPath("$.discount", is(20.0))) // Discount from previous update
                    .andExpect(jsonPath("$.finalPrice", is(400.00))); // 500.00 - 20% = 400.00
        }

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .price(299.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", nonExistentId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            // Given
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .price(299.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when name is too short")
        void shouldReturn400WhenNameIsTooShort() throws Exception {
            // Given
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("AB") // Less than 3 characters
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is zero")
        void shouldReturn400WhenPriceIsZero() throws Exception {
            // Given
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .price(0.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price exceeds maximum")
        void shouldReturn400WhenPriceExceedsMaximum() throws Exception {
            // Given
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .price(10_000_000.0) // Exceeds max
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when discount exceeds 100%")
        void shouldReturn400WhenDiscountExceeds100() throws Exception {
            // Given
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .discount(150.0) // Exceeds 100%
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when description exceeds max length")
        void shouldReturn400WhenDescriptionTooLong() throws Exception {
            // Given
            String veryLongDescription = "a".repeat(2001); // Exceeds 2000 characters

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .description(veryLongDescription)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when count is negative")
        void shouldReturn400WhenCountIsNegative() throws Exception {
            // Given
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .count(-5)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should update product with null optional fields")
        void shouldUpdateWithNullOptionalFields() throws Exception {
            // Given - Set some fields to null
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .brand(null)
                    .shortDescription(null)
                    .description(null)
                    .keywords(null)
                    .price(299.99)
                    .categoryId(null)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    // Fields must remain old
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(updateRequest.getName())))
                    .andExpect(jsonPath("$.brand", is(originalProduct.getBrand())))
                    .andExpect(jsonPath("$.shortDescription", is(originalProduct.getShortDescription())))
                    .andExpect(jsonPath("$.description", is(originalProduct.getDescription())))
                    .andExpect(jsonPath("$.keywords", is(originalProduct.getKeywords())))
                    .andExpect(jsonPath("$.price", is(299.99)))
                    .andExpect(jsonPath("$.categoryId", is(originalProduct.getCategoryId().toString())));

            // Verify in database
            Product updatedProduct = productRepository.findById(existingProductId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getBrand()).isEqualTo(originalProduct.getBrand());
            assertThat(updatedProduct.getShortDescription()).isEqualTo(originalProduct.getShortDescription());
            assertThat(updatedProduct.getDescription()).isEqualTo(originalProduct.getDescription());
            assertThat(updatedProduct.getKeywords()).isEqualTo(originalProduct.getKeywords());
            assertThat(updatedProduct.getCategoryId()).isEqualTo(originalProduct.getCategoryId());
        }

        @Test
        @DisplayName("Should maintain audit fields after update")
        void shouldMaintainAuditFieldsAfterUpdate() throws Exception {
            // Given
            String originalCreatedAt = originalProduct.getCreatedAt();
            String originalLastModifiedAt = originalProduct.getLastModifiedAt();

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .price(299.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // Wait a moment to ensure timestamp difference
            Thread.sleep(10);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.createdAt", is(originalCreatedAt))) // Created date should not change
                    .andExpect(jsonPath("$.lastModifiedAt", not(originalLastModifiedAt))); // Modified date should update
        }

        @Test
        @DisplayName("Should update available status based on count")
        void shouldUpdateAvailableStatusBasedOnCount() throws Exception {
            // Given - Update count to zero
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .count(0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.count", is(0)))
                    .andExpect(jsonPath("$.available", is(false)));

            // Given - Update count to positive
            updateRequest = UpdateProductRequest.builder()
                    .count(10)
                    .build();

            requestBody = objectMapper.writeValueAsString(updateRequest);

            // When & Then
            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.count", is(10)))
                    .andExpect(jsonPath("$.available", is(false)));
        }
    }

    @Nested
    @DisplayName("POST /products/decrease-stock - Decrease Stock")
    class DecreaseStockTests {

        private UUID productId;
        private CreateProductRequest productRequest;
        private int initialCount;

        @BeforeEach
        void setUp() throws Exception {
            initialCount = 50;

            productRequest = CreateProductRequest.builder()
                    .name("Test Product for Stock Decrease")
                    .brand("TestBrand")
                    .shortDescription("Product for testing stock decrease")
                    .description("Full description for stock decrease testing")
                    .keywords("stock, decrease, test")
                    .price(299.99)
                    .count(initialCount)
                    .discount(0.0)
                    .categoryId(UUID.randomUUID())
                    .build();

            ProductResponse createdProduct = createTestProduct(productRequest);
            productId = createdProduct.getId();
        }

        @Test
        @DisplayName("Should successfully decrease stock by valid quantity")
        void shouldDecreaseStockSuccessfully() throws Exception {
            // Given
            int decreaseQuantity = 5;
            int expectedCount = initialCount - decreaseQuantity;

            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.name", is(productRequest.getName())))
                    .andExpect(jsonPath("$.count", is(expectedCount)))
                    .andExpect(jsonPath("$.available", is(false)));

            // Verify in database
            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("Should decrease stock to zero and update available status")
        void shouldDecreaseStockToZeroAndUpdateAvailable() throws Exception {
            // Given
            int decreaseQuantity = initialCount; // Decrease all stock
            int expectedCount = 0;

            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(expectedCount)))
                    .andExpect(jsonPath("$.available", is(false))); // Should be false when count is 0

            // Verify in database
            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isZero();
        }

        @Test
        @DisplayName("Should return 400 Bad Request when decreasing more than available stock")
        void shouldReturn400WhenDecreasingMoreThanAvailable() throws Exception {
            // Given
            int decreaseQuantity = initialCount + 10; // More than available

            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isBadRequest());

            // Verify stock was not changed
            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when decreasing with negative quantity")
        void shouldReturn400WhenDecreasingWithNegativeQuantity() throws Exception {
            // Given
            int negativeQuantity = -5;

            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(negativeQuantity)))
                    .andExpect(status().isBadRequest());

            // Verify stock was not changed
            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when decreasing with zero quantity")
        void shouldReturn400WhenDecreasingWithZeroQuantity() throws Exception {
            // Given
            int zeroQuantity = 0;

            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(zeroQuantity)))
                    .andExpect(status().isBadRequest());

            // Verify stock was not changed
            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 404 Not Found when product does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            int decreaseQuantity = 5;

            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", nonExistentId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            // Given
            int decreaseQuantity = 5;

            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when quantity parameter is missing")
        void shouldReturn400WhenQuantityParameterMissing() throws Exception {
            // When & Then
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /products/increase-stock - Increase Stock")
    class IncreaseStockTests {

        private UUID productId;
        private CreateProductRequest productRequest;
        private int initialCount;

        @BeforeEach
        void setUp() throws Exception {
            initialCount = 25;

            productRequest = CreateProductRequest.builder()
                    .name("Test Product for Stock Increase")
                    .brand("TestBrand")
                    .shortDescription("Product for testing stock increase")
                    .description("Full description for stock increase testing")
                    .keywords("stock, increase, test")
                    .price(199.99)
                    .count(initialCount)
                    .discount(0.0)
                    .categoryId(UUID.randomUUID())
                    .build();

            ProductResponse createdProduct = createTestProduct(productRequest);
            productId = createdProduct.getId();
        }

        @Test
        @DisplayName("Should successfully increase stock by valid quantity")
        void shouldIncreaseStockSuccessfully() throws Exception {
            // Given
            int increaseQuantity = 10;
            int expectedCount = initialCount + increaseQuantity;

            // When & Then
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.name", is(productRequest.getName())))
                    .andExpect(jsonPath("$.count", is(expectedCount)))
                    .andExpect(jsonPath("$.available", is(false))); // Should remain true

            // Verify in database
            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("Should increase stock from zero and update available status")
        void shouldIncreaseStockFromZeroAndUpdateAvailable() throws Exception {
            // Given - First decrease to zero
            int decreaseQuantity = initialCount;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(0)))
                    .andExpect(jsonPath("$.available", is(false)));

            // When - Increase stock
            int increaseQuantity = 15;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(increaseQuantity)))
                    // Attention - after add moderating this test will be failed
                    .andExpect(jsonPath("$.available", is(true))); // Should become true again

            // Verify in database
            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(increaseQuantity);
        }

        @Test
        @DisplayName("Should successfully increase stock by large quantity")
        void shouldIncreaseStockByLargeQuantity() throws Exception {
            // Given
            int largeIncreaseQuantity = 999_000; // Large but within max limit (999,999)
            int expectedCount = initialCount + largeIncreaseQuantity;

            // When & Then
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(largeIncreaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(expectedCount)));

            // Verify in database
            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when increasing with negative quantity")
        void shouldReturn400WhenIncreasingWithNegativeQuantity() throws Exception {
            // Given
            int negativeQuantity = -5;

            // When & Then
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(negativeQuantity)))
                    .andExpect(status().isBadRequest());

            // Verify stock was not changed
            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when increasing with zero quantity")
        void shouldReturn400WhenIncreasingWithZeroQuantity() throws Exception {
            // Given
            int zeroQuantity = 0;

            // When & Then
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(zeroQuantity)))
                    .andExpect(status().isBadRequest());

            // Verify stock was not changed
            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 404 Not Found when product does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            int increaseQuantity = 5;

            // When & Then
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", nonExistentId.toString())
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            // Given
            int increaseQuantity = 5;

            // When & Then
            mockMvc.perform(post("/products/increase-stock")
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when quantity parameter is missing")
        void shouldReturn400WhenQuantityParameterMissing() throws Exception {
            // When & Then
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle multiple stock operations sequentially")
        void shouldHandleMultipleStockOperations() throws Exception {
            // Given - Multiple operations
            int firstIncrease = 10;
            int decrease1 = 5;
            int decrease2 = 8;
            int secondIncrease = 20;

            int expectedCount = initialCount + firstIncrease - decrease1 - decrease2 + secondIncrease;

            // When & Then - Perform operations in sequence
            // First increase
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(firstIncrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease)));

            // First decrease
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decrease1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease - decrease1)));

            // Second decrease
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decrease2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease - decrease1 - decrease2)));

            // Second increase - final check
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(secondIncrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(expectedCount)));

            // Final verification in database
            Product finalProduct = productRepository.findById(productId).orElse(null);
            assertThat(finalProduct).isNotNull();
            assertThat(finalProduct.getCount()).isEqualTo(expectedCount);
        }
    }

}