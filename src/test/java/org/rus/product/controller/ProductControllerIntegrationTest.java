package org.rus.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rus.product.domain.Product;
import org.rus.product.dto.CreateProductRequest;
import org.rus.product.dto.ProductResponse;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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