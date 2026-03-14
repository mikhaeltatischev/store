package org.rus.product.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rus.product.dto.CreateProductRequest;
import org.rus.product.dto.ProductResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GET /products - Get Product by ID Tests")
public class ProductGetByIdControllerTest extends BaseProductControllerTest {

    private UUID existingProductId;
    private CreateProductRequest productRequest;

    @BeforeEach
    void setUp() throws Exception {
        UUID categoryId = UUID.randomUUID();

        productRequest = CreateProductRequest.builder()
                .name("Test Product for Retrieval")
                .brand("TestBrand")
                .shortDescription("Short description for test product")
                .description("Full description for test product with all details")
                .keywords("test, product, integration")
                .price(499.99)
                .count(25)
                .discount(15.0)
                .categoryId(categoryId)
                .build();

        String requestBody = objectMapper.writeValueAsString(productRequest);

        MvcResult result = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        ProductResponse createdProductResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResponse.class
        );

        existingProductId = createdProductResponse.getId();
    }

    @Nested
    @DisplayName("Success scenarios")
    class GetProductByIdSuccessTests {

        @Test
        @DisplayName("Should successfully retrieve product by ID")
        void shouldGetProductByIdSuccessfully() throws Exception {
            mockMvc.perform(get("/products")
                            .param("id", existingProductId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(productRequest.getName())))
                    .andExpect(jsonPath("$.brand", is(productRequest.getBrand())))
                    .andExpect(jsonPath("$.shortDescription", is(productRequest.getShortDescription())))
                    .andExpect(jsonPath("$.description", is(productRequest.getDescription())))
                    .andExpect(jsonPath("$.keywords", is(productRequest.getKeywords())))
                    .andExpect(jsonPath("$.price", is(productRequest.getPrice())))
                    .andExpect(jsonPath("$.currency", is("RUB")))
                    .andExpect(jsonPath("$.finalPrice", is(424.99)))
                    .andExpect(jsonPath("$.count", is(productRequest.getCount())))
                    .andExpect(jsonPath("$.discount", is(productRequest.getDiscount())))
                    .andExpect(jsonPath("$.categoryId", is(productRequest.getCategoryId().toString())))
                    .andExpect(jsonPath("$.creatorId", notNullValue()))
                    .andExpect(jsonPath("$.status", is("ACTIVE")))
                    .andExpect(jsonPath("$.available", is(true)))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.lastModifiedAt", notNullValue()));
        }

        @Test
        @DisplayName("Should retrieve multiple different products by their IDs")
        void shouldRetrieveMultipleProductsById() throws Exception {
            UUID secondCategoryId = UUID.randomUUID();

            CreateProductRequest secondProductRequest = CreateProductRequest.builder()
                    .name("Second Test Product")
                    .brand("AnotherBrand")
                    .shortDescription("Another test product")
                    .description("Description for second test product")
                    .keywords("second, test")
                    .price(299.99)
                    .count(10)
                    .discount(0.0)
                    .categoryId(secondCategoryId)
                    .build();

            String secondRequestBody = objectMapper.writeValueAsString(secondProductRequest);

            MvcResult secondResult = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(secondRequestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            ProductResponse secondCreatedProduct = objectMapper.readValue(
                    secondResult.getResponse().getContentAsString(),
                    ProductResponse.class
            );

            mockMvc.perform(get("/products")
                            .param("id", existingProductId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(productRequest.getName())));

            mockMvc.perform(get("/products")
                            .param("id", secondCreatedProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(secondCreatedProduct.getId().toString())))
                    .andExpect(jsonPath("$.name", is(secondProductRequest.getName())));
        }

        @Test
        @DisplayName("Should return product with correct available status when count is zero")
        void shouldReturnProductWithAvailableFalseWhenCountZero() throws Exception {
            CreateProductRequest zeroCountRequest = CreateProductRequest.builder()
                    .name("Out of Stock Product")
                    .brand("TestBrand")
                    .price(99.99)
                    .count(0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(zeroCountRequest);

            MvcResult result = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            ProductResponse zeroCountProduct = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ProductResponse.class
            );

            mockMvc.perform(get("/products")
                            .param("id", zeroCountProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(zeroCountProduct.getId().toString())))
                    .andExpect(jsonPath("$.name", is(zeroCountRequest.getName())))
                    .andExpect(jsonPath("$.count", is(0)))
                    .andExpect(jsonPath("$.available", is(false)));
        }

        @Test
        @DisplayName("Should return product with correct final price calculation when discount applied")
        void shouldReturnProductWithCorrectFinalPrice() throws Exception {
            CreateProductRequest discountProductRequest = CreateProductRequest.builder()
                    .name("Discounted Product")
                    .brand("TestBrand")
                    .price(1000.00)
                    .count(5)
                    .discount(25.5)
                    .build();

            String requestBody = objectMapper.writeValueAsString(discountProductRequest);

            MvcResult result = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            ProductResponse discountProduct = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ProductResponse.class
            );

            mockMvc.perform(get("/products")
                            .param("id", discountProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(discountProduct.getId().toString())))
                    .andExpect(jsonPath("$.price", is(1000.00)))
                    .andExpect(jsonPath("$.discount", is(25.5)))
                    .andExpect(jsonPath("$.finalPrice", is(745.00)));
        }

        @Test
        @DisplayName("Should return product with null optional fields when they were not provided")
        void shouldReturnProductWithNullOptionalFields() throws Exception {
            CreateProductRequest minimalRequest = CreateProductRequest.builder()
                    .name("Minimal Product")
                    .price(199.99)
                    .count(0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(minimalRequest);

            MvcResult result = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            ProductResponse minimalProduct = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ProductResponse.class
            );

            mockMvc.perform(get("/products")
                            .param("id", minimalProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(minimalProduct.getId().toString())))
                    .andExpect(jsonPath("$.name", is(minimalRequest.getName())))
                    .andExpect(jsonPath("$.brand", nullValue()))
                    .andExpect(jsonPath("$.shortDescription", nullValue()))
                    .andExpect(jsonPath("$.description", nullValue()))
                    .andExpect(jsonPath("$.keywords", nullValue()))
                    .andExpect(jsonPath("$.price", is(199.99)))
                    .andExpect(jsonPath("$.finalPrice", is(199.99)))
                    .andExpect(jsonPath("$.count", is(0)))
                    .andExpect(jsonPath("$.discount", is(0.0)))
                    .andExpect(jsonPath("$.categoryId", nullValue()));
        }
    }

    @Nested
    @DisplayName("Error scenarios")
    class GetProductByIdErrorTests {

        @Test
        @DisplayName("Should return 404 Not Found when product does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/products")
                            .param("id", nonExistentId.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            mockMvc.perform(get("/products"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID is invalid UUID format")
        void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
            mockMvc.perform(get("/products")
                            .param("id", "invalid-uuid-format"))
                    .andExpect(status().isBadRequest());
        }
    }

}