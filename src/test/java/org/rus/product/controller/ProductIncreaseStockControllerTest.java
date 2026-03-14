package org.rus.product.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rus.product.domain.Product;
import org.rus.product.dto.CreateProductRequest;
import org.rus.product.dto.ProductResponse;
import org.rus.product.entity.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("POST /products/increase-stock - Increase Stock Tests")
public class ProductIncreaseStockControllerTest extends BaseProductControllerTest {

    @Autowired
    private ProductRepository productRepository;

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

        String requestBody = objectMapper.writeValueAsString(productRequest);

        MvcResult createResult = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        ProductResponse createdProduct = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductResponse.class
        );

        productId = createdProduct.getId();
    }

    @Nested
    @DisplayName("Success scenarios")
    class IncreaseStockSuccessTests {

        @Test
        @DisplayName("Should successfully increase stock by valid quantity")
        void shouldIncreaseStockSuccessfully() throws Exception {
            int increaseQuantity = 10;
            int expectedCount = initialCount + increaseQuantity;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.name", is(productRequest.getName())))
                    .andExpect(jsonPath("$.count", is(expectedCount)))
                    .andExpect(jsonPath("$.available", is(true)));

            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("Should increase stock from zero and update available status")
        void shouldIncreaseStockFromZeroAndUpdateAvailable() throws Exception {
            int decreaseQuantity = initialCount;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(0)))
                    .andExpect(jsonPath("$.available", is(false)));

            int increaseQuantity = 15;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(increaseQuantity)))
                    .andExpect(jsonPath("$.available", is(true)));

            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(increaseQuantity);
        }

        @Test
        @DisplayName("Should successfully increase stock by large quantity")
        void shouldIncreaseStockByLargeQuantity() throws Exception {
            int largeIncreaseQuantity = 999_000;
            int expectedCount = initialCount + largeIncreaseQuantity;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(largeIncreaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(expectedCount)));

            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("Should allow multiple stock increases")
        void shouldAllowMultipleStockIncreases() throws Exception {
            int firstIncrease = 10;
            int secondIncrease = 15;
            int expectedCount = initialCount + firstIncrease + secondIncrease;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(firstIncrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease)));

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(secondIncrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease + secondIncrease)));

            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(expectedCount);
        }
    }

    @Nested
    @DisplayName("Error scenarios")
    class IncreaseStockErrorTests {

        @Test
        @DisplayName("Should return 400 Bad Request when increasing with negative quantity")
        void shouldReturn400WhenIncreasingWithNegativeQuantity() throws Exception {
            int negativeQuantity = -5;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(negativeQuantity)))
                    .andExpect(status().isBadRequest());

            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when increasing with zero quantity")
        void shouldReturn400WhenIncreasingWithZeroQuantity() throws Exception {
            int zeroQuantity = 0;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(zeroQuantity)))
                    .andExpect(status().isBadRequest());

            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 404 Not Found when product does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            int increaseQuantity = 5;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", nonExistentId.toString())
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            int increaseQuantity = 5;

            mockMvc.perform(post("/products/increase-stock")
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when quantity parameter is missing")
        void shouldReturn400WhenQuantityParameterMissing() throws Exception {
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when quantity is not a number")
        void shouldReturn400WhenQuantityIsNotANumber() throws Exception {
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", "not-a-number"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID is invalid UUID")
        void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
            int increaseQuantity = 5;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", "invalid-uuid")
                            .param("quantity", String.valueOf(increaseQuantity)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should not change stock when operation fails")
        void shouldNotChangeStockWhenOperationFails() throws Exception {
            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", "-5"))
                    .andExpect(status().isBadRequest());

            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", "invalid-uuid")
                            .param("quantity", "10"))
                    .andExpect(status().isBadRequest());

            unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }
    }

    @Nested
    @DisplayName("Integration scenarios")
    class IncreaseStockIntegrationTests {

        @Test
        @DisplayName("Should handle multiple stock operations sequentially")
        void shouldHandleMultipleStockOperations() throws Exception {
            int firstIncrease = 10;
            int decrease1 = 5;
            int decrease2 = 8;
            int secondIncrease = 20;

            int expectedCount = initialCount + firstIncrease - decrease1 - decrease2 + secondIncrease;

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(firstIncrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease)));

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decrease1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease - decrease1)));

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decrease2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount + firstIncrease - decrease1 - decrease2)));

            mockMvc.perform(post("/products/increase-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(secondIncrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(expectedCount)));

            Product finalProduct = productRepository.findById(productId).orElse(null);
            assertThat(finalProduct).isNotNull();
            assertThat(finalProduct.getCount()).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("Should maintain data integrity under concurrent-like operations")
        void shouldMaintainDataIntegrity() throws Exception {
            int operations = 5;
            int increaseAmount = 10;
            int decreaseAmount = 5;

            int expectedCount = initialCount;

            for (int i = 0; i < operations; i++) {
                mockMvc.perform(post("/products/increase-stock")
                                .param("id", productId.toString())
                                .param("quantity", String.valueOf(increaseAmount)))
                        .andExpect(status().isOk());
                expectedCount += increaseAmount;

                mockMvc.perform(post("/products/decrease-stock")
                                .param("id", productId.toString())
                                .param("quantity", String.valueOf(decreaseAmount)))
                        .andExpect(status().isOk());
                expectedCount -= decreaseAmount;
            }

            Product finalProduct = productRepository.findById(productId).orElse(null);
            assertThat(finalProduct).isNotNull();
            assertThat(finalProduct.getCount()).isEqualTo(expectedCount);
        }
    }

}