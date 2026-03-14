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

@DisplayName("POST /products/decrease-stock - Decrease Stock Tests")
public class ProductDecreaseStockControllerTest extends BaseProductControllerTest {

    @Autowired
    private ProductRepository productRepository;

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
    class DecreaseStockSuccessTests {

        @Test
        @DisplayName("Should successfully decrease stock by valid quantity")
        void shouldDecreaseStockSuccessfully() throws Exception {
            int decreaseQuantity = 5;
            int expectedCount = initialCount - decreaseQuantity;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
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
        @DisplayName("Should decrease stock to zero and update available status")
        void shouldDecreaseStockToZeroAndUpdateAvailable() throws Exception {
            int decreaseQuantity = initialCount;
            int expectedCount = 0;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(productId.toString())))
                    .andExpect(jsonPath("$.count", is(expectedCount)))
                    .andExpect(jsonPath("$.available", is(false)));

            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isZero();
        }

        @Test
        @DisplayName("Should allow multiple stock decreases")
        void shouldAllowMultipleStockDecreases() throws Exception {
            int firstDecrease = 10;
            int secondDecrease = 15;
            int expectedCount = initialCount - firstDecrease - secondDecrease;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(firstDecrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount - firstDecrease)));

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(secondDecrease)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(initialCount - firstDecrease - secondDecrease)));

            Product updatedProduct = productRepository.findById(productId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getCount()).isEqualTo(expectedCount);
        }
    }

    @Nested
    @DisplayName("Error scenarios")
    class DecreaseStockErrorTests {

        @Test
        @DisplayName("Should return 400 Bad Request when decreasing more than available stock")
        void shouldReturn400WhenDecreasingMoreThanAvailable() throws Exception {
            int decreaseQuantity = initialCount + 10;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isBadRequest());

            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when decreasing with negative quantity")
        void shouldReturn400WhenDecreasingWithNegativeQuantity() throws Exception {
            int negativeQuantity = -5;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(negativeQuantity)))
                    .andExpect(status().isBadRequest());

            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when decreasing with zero quantity")
        void shouldReturn400WhenDecreasingWithZeroQuantity() throws Exception {
            int zeroQuantity = 0;

            mockMvc.perform(post("/products/decrease-stock")
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
            int decreaseQuantity = 5;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", nonExistentId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            int decreaseQuantity = 5;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when quantity parameter is missing")
        void shouldReturn400WhenQuantityParameterMissing() throws Exception {
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when quantity is not a number")
        void shouldReturn400WhenQuantityIsNotANumber() throws Exception {
            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", "not-a-number"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID is invalid UUID")
        void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
            int decreaseQuantity = 5;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", "invalid-uuid")
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should not change stock when operation fails")
        void shouldNotChangeStockWhenOperationFails() throws Exception {
            int decreaseQuantity = initialCount + 10;

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", String.valueOf(decreaseQuantity)))
                    .andExpect(status().isBadRequest());

            Product unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);

            mockMvc.perform(post("/products/decrease-stock")
                            .param("id", productId.toString())
                            .param("quantity", "-5"))
                    .andExpect(status().isBadRequest());

            unchangedProduct = productRepository.findById(productId).orElse(null);
            assertThat(unchangedProduct).isNotNull();
            assertThat(unchangedProduct.getCount()).isEqualTo(initialCount);
        }
    }

}