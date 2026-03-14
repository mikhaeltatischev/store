package org.rus.product.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PUT /products - Update Product Tests")
public class ProductUpdateControllerTest extends BaseProductControllerTest {

    @Autowired
    private ProductRepository productRepository;

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

        existingProductId = createFullTestProduct(originalProductRequest);

        MvcResult result = mockMvc.perform(get("/products")
                        .param("id", existingProductId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        originalProduct = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResponse.class
        );
    }

    @Nested
    @DisplayName("Success scenarios")
    class UpdateProductSuccessTests {

        @Test
        @DisplayName("Should successfully update all product fields")
        void shouldUpdateAllProductFieldsSuccessfully() throws Exception {
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
                    .andExpect(jsonPath("$.finalPrice", is(269.99)))
                    .andExpect(jsonPath("$.count", is(updateRequest.getCount())))
                    .andExpect(jsonPath("$.discount", is(updateRequest.getDiscount())))
                    .andExpect(jsonPath("$.categoryId", is(updateRequest.getCategoryId().toString())))
                    .andExpect(jsonPath("$.creatorId", is(originalProduct.getCreatorId().toString())))
                    .andExpect(jsonPath("$.status", is("ACTIVE")))
                    .andExpect(jsonPath("$.available", is(true)))
                    .andExpect(jsonPath("$.createdAt", is(originalProduct.getCreatedAt())))
                    .andExpect(jsonPath("$.lastModifiedAt", not(originalProduct.getLastModifiedAt())));

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
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Only Name Updated")
                    .price(399.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(updateRequest.getName())))
                    .andExpect(jsonPath("$.brand", is(originalProductRequest.getBrand())))
                    .andExpect(jsonPath("$.shortDescription", is(originalProductRequest.getShortDescription())))
                    .andExpect(jsonPath("$.description", is(originalProductRequest.getDescription())))
                    .andExpect(jsonPath("$.keywords", is(originalProductRequest.getKeywords())))
                    .andExpect(jsonPath("$.price", is(updateRequest.getPrice())))
                    .andExpect(jsonPath("$.count", is(originalProductRequest.getCount())))
                    .andExpect(jsonPath("$.discount", is(originalProductRequest.getDiscount())))
                    .andExpect(jsonPath("$.categoryId", is(originalProductRequest.getCategoryId().toString())));

            Product updatedProduct = productRepository.findById(existingProductId).orElse(null);
            assertThat(updatedProduct).isNotNull();
            assertThat(updatedProduct.getName()).isEqualTo(updateRequest.getName());
            assertThat(updatedProduct.getPrice().getAmount()).isEqualTo(BigDecimal.valueOf(updateRequest.getPrice()));
            assertThat(updatedProduct.getBrand()).isEqualTo(originalProductRequest.getBrand());
        }

        @Test
        @DisplayName("Should update product and recalculate final price correctly")
        void shouldRecalculateFinalPriceAfterUpdate() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .discount(20.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.price", is(originalProductRequest.getPrice())))
                    .andExpect(jsonPath("$.discount", is(20.0)))
                    .andExpect(jsonPath("$.finalPrice", is(159.99)));

            updateRequest = UpdateProductRequest.builder()
                    .price(500.00)
                    .build();

            requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.price", is(500.00)))
                    .andExpect(jsonPath("$.discount", is(20.0)))
                    .andExpect(jsonPath("$.finalPrice", is(400.00)));
        }

        @Test
        @DisplayName("Should update product with null optional fields")
        void shouldUpdateWithNullOptionalFields() throws Exception {
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

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(updateRequest.getName())))
                    .andExpect(jsonPath("$.brand", is(originalProduct.getBrand())))
                    .andExpect(jsonPath("$.shortDescription", is(originalProduct.getShortDescription())))
                    .andExpect(jsonPath("$.description", is(originalProduct.getDescription())))
                    .andExpect(jsonPath("$.keywords", is(originalProduct.getKeywords())))
                    .andExpect(jsonPath("$.price", is(299.99)))
                    .andExpect(jsonPath("$.categoryId", is(originalProduct.getCategoryId().toString())));

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
            String originalCreatedAt = originalProduct.getCreatedAt();
            String originalLastModifiedAt = originalProduct.getLastModifiedAt();

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .price(299.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            Thread.sleep(10);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.createdAt", is(originalCreatedAt)))
                    .andExpect(jsonPath("$.lastModifiedAt", not(originalLastModifiedAt)));
        }

        @Test
        @DisplayName("Should update available status based on count")
        void shouldUpdateAvailableStatusBasedOnCount() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .count(0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.count", is(0)))
                    .andExpect(jsonPath("$.available", is(false)));

            updateRequest = UpdateProductRequest.builder()
                    .count(10)
                    .build();

            requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.count", is(10)))
                    .andExpect(jsonPath("$.available", is(true)));
        }
    }

    @Nested
    @DisplayName("Error scenarios")
    class UpdateProductErrorTests {

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existent product")
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .price(299.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", nonExistentId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .price(299.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when name is too short")
        void shouldReturn400WhenNameIsTooShort() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .name("AB")
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is zero")
        void shouldReturn400WhenPriceIsZero() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .price(0.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price exceeds maximum")
        void shouldReturn400WhenPriceExceedsMaximum() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .price(10_000_000.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when discount exceeds 100%")
        void shouldReturn400WhenDiscountExceeds100() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .discount(150.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when description exceeds max length")
        void shouldReturn400WhenDescriptionTooLong() throws Exception {
            String veryLongDescription = "a".repeat(2001);

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .description(veryLongDescription)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when count is negative")
        void shouldReturn400WhenCountIsNegative() throws Exception {
            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .count(-5)
                    .build();

            String requestBody = objectMapper.writeValueAsString(updateRequest);

            mockMvc.perform(put("/products")
                            .param("id", existingProductId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

}