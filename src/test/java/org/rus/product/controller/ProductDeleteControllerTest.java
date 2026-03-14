package org.rus.product.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rus.product.dto.PageResponse;
import org.rus.product.dto.ProductSummaryResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Delete Product Tests")
public class ProductDeleteControllerTest extends BaseProductControllerTest {

    private UUID productToDeleteId;
    private UUID anotherProductId;

    @BeforeEach
    void setUpProducts() throws Exception {
        productToDeleteId = createTestProduct("Product to Delete", 100.0, 10, null);
        anotherProductId = createTestProduct("Another Product", 200.0, 5, null);
    }

    @Nested
    @DisplayName("DELETE /products - Success scenarios")
    class DeleteProductSuccessTests {

        @Test
        @DisplayName("Should successfully soft delete existing product")
        void shouldSoftDeleteExistingProduct() throws Exception {
            mockMvc.perform(delete("/products")
                            .param("id", productToDeleteId.toString()))
                    .andExpect(status().isNoContent());

            MvcResult result = mockMvc.perform(get("/products/available"))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            PageResponse<ProductSummaryResponse> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {}
            );

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotNull();

            assertAll(
                    "Verify product deletion",
                    () -> assertThat(response.getContent())
                            .extracting(ProductSummaryResponse::getId)
                            .doesNotContain(productToDeleteId),
                    () -> assertThat(response.getContent())
                            .extracting(ProductSummaryResponse::getId)
                            .contains(anotherProductId)
            );
        }

        @Test
        @DisplayName("Should allow deleting multiple products")
        void shouldAllowDeletingMultipleProducts() throws Exception {
            // Create another product for delete
            UUID extraProductId = createTestProduct("Extra Product", 300.0, 3, null);

            mockMvc.perform(delete("/products")
                            .param("id", productToDeleteId.toString()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete("/products")
                            .param("id", extraProductId.toString()))
                    .andExpect(status().isNoContent());

            MvcResult result = mockMvc.perform(get("/products/available"))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            PageResponse<ProductSummaryResponse> response = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {}
            );

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isNotNull();

            assertAll(
                    "Verify both products are deleted",
                    () -> assertThat(response.getContent())
                            .extracting(ProductSummaryResponse::getId)
                            .doesNotContain(productToDeleteId),
                    () -> assertThat(response.getContent())
                            .extracting(ProductSummaryResponse::getId)
                            .doesNotContain(extraProductId),
                    () -> assertThat(response.getContent())
                            .extracting(ProductSummaryResponse::getId)
                            .contains(anotherProductId)
            );
        }
    }

    @Nested
    @DisplayName("DELETE /products - Error scenarios")
    class DeleteProductErrorTests {

        @Test
        @DisplayName("Should return 400 when product ID is null")
        void shouldReturnBadRequestWhenIdIsNull() throws Exception {
            mockMvc.perform(delete("/products"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(delete("/products")
                            .param("id", nonExistentId.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when deleting already deleted product")
        void shouldReturnNotFoundWhenDeletingAlreadyDeletedProduct() throws Exception {
            mockMvc.perform(delete("/products")
                            .param("id", productToDeleteId.toString()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete("/products")
                            .param("id", productToDeleteId.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void shouldReturnBadRequestForInvalidUuidFormat() throws Exception {
            mockMvc.perform(delete("/products")
                            .param("id", "invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

}