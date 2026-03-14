package org.rus.product.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rus.product.dto.PageResponse;
import org.rus.product.dto.ProductSummaryResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Get Available Products Tests")
public class ProductGetAvailableControllerTest extends BaseProductControllerTest {

    @BeforeEach
    void setUpProducts() throws Exception {
        createTestProduct("Available Product", 100.0, 10, null);
        createTestProduct("Out of Stock Product", 200.0, 0, null);
        createTestProduct("Discounted Product", 300.0, 5, 15.0);

        createTestProduct("Available Product 2", 150.0, 8, null);
        createTestProduct("Available Product 3", 250.0, 12, null);
        createTestProduct("Available Product 4", 350.0, 3, null);
        createTestProduct("Available Product 5", 450.0, 20, null);
    }

    @Nested
    @DisplayName("GET /products/available - Success scenarios")
    class GetAvailableProductsSuccessTests {

        @Test
        @DisplayName("Should return available products with default pagination")
        void shouldReturnAvailableProductsWithDefaultPagination() throws Exception {
            MvcResult result = mockMvc.perform(get("/products/available"))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            PageResponse<ProductSummaryResponse> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {
                    }
            );

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotNull();
            assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(5);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
            assertThat(response.getTotalPages()).isGreaterThan(0);

            // We check that the unavailable product is not included in the results
            assertThat(response.getContent())
                    .extracting(ProductSummaryResponse::getName)
                    .doesNotContain("Out of Stock Product");
        }

        @Test
        @DisplayName("Should return available products with custom pagination")
        void shouldReturnAvailableProductsWithCustomPagination() throws Exception {
            MvcResult result = mockMvc.perform(get("/products/available")
                            .param("page", "1")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            PageResponse<ProductSummaryResponse> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {
                    }
            );

            assertThat(response).isNotNull();
            assertThat(response.getPage()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(2);

            assertThat(response.getContent()).isNotNull();
            assertThat(response.getContent().size()).isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should return correct product summary information")
        void shouldReturnCorrectProductSummary() throws Exception {
            MvcResult result = mockMvc.perform(get("/products/available"))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            PageResponse<ProductSummaryResponse> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {
                    }
            );

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotNull();

            // Found discounted product
            ProductSummaryResponse discountedProduct = response.getContent().stream()
                    .filter(p -> p.getName().equals("Discounted Product"))
                    .findFirst()
                    .orElse(null);

            assertThat(discountedProduct).isNotNull();
            assertAll(
                    "Verify product summary fields",
                    () -> assertThat(discountedProduct.getId()).isNotNull(),
                    () -> assertThat(discountedProduct.getName()).isEqualTo("Discounted Product"),
                    () -> assertThat(discountedProduct.getFinalPrice()).isNotNull(),
                    () -> assertThat(discountedProduct.getFinalPrice()).isNotNull(),
                    () -> assertThat(discountedProduct.getCurrency()).isEqualTo("RUB")
            );
        }
    }

    @Nested
    @DisplayName("GET /products/available - Edge cases")
    class GetAvailableProductsEdgeCases {

        @Test
        @DisplayName("Should not return products with zero count")
        void shouldNotReturnProductsWithZeroCount() throws Exception {
            MvcResult result = mockMvc.perform(get("/products/available"))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            PageResponse<ProductSummaryResponse> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {
                    }
            );

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotNull();

            assertThat(response.getContent())
                    .extracting(ProductSummaryResponse::getName)
                    .doesNotContain("Out of Stock Product");
        }

        @Test
        @DisplayName("Should handle negative page number as 0")
        void shouldHandleNegativePageAsZero() throws Exception {
            MvcResult result = mockMvc.perform(get("/products/available")
                            .param("page", "-5"))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            PageResponse<ProductSummaryResponse> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {
                    }
            );

            assertThat(response).isNotNull();
            assertThat(response.getPage()).isEqualTo(0);
        }
    }

}