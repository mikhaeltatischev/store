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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("POST /products - Create Product Tests")
public class ProductCreateControllerTest extends BaseProductControllerTest {

    @Autowired
    private ProductRepository productRepository;

    private CreateProductRequest validRequest;

    @BeforeEach
    void init() {
        validRequest = CreateProductRequest.builder()
                .name("iPhone 15 Pro")
                .brand("Apple")
                .shortDescription("Latest flagship smartphone")
                .description("The iPhone 15 Pro features an advanced camera system, A17 Pro chip, and titanium design")
                .keywords("iphone, smartphone, apple, ios")
                .price(999.99)
                .count(50)
                .discount(10.0)
                .categoryId(UUID.randomUUID())
                .build();
    }

    @Nested
    @DisplayName("Success scenarios")
    class CreateProductSuccessTests {

        @Test
        @DisplayName("Should successfully create a product with valid data")
        void shouldCreateProductSuccessfully() throws Exception {
            String requestBody = objectMapper.writeValueAsString(validRequest);

            MvcResult result = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is(validRequest.getName())))
                    .andExpect(jsonPath("$.brand", is(validRequest.getBrand())))
                    .andExpect(jsonPath("$.shortDescription", is(validRequest.getShortDescription())))
                    .andExpect(jsonPath("$.description", is(validRequest.getDescription())))
                    .andExpect(jsonPath("$.keywords", is(validRequest.getKeywords())))
                    .andExpect(jsonPath("$.price", is(999.99)))
                    .andExpect(jsonPath("$.currency", is("RUB")))
                    .andExpect(jsonPath("$.finalPrice", is(899.99)))
                    .andExpect(jsonPath("$.count", is(validRequest.getCount())))
                    .andExpect(jsonPath("$.discount", is(validRequest.getDiscount())))
                    .andExpect(jsonPath("$.categoryId", is(validRequest.getCategoryId().toString())))
                    .andExpect(jsonPath("$.creatorId", notNullValue()))
                    .andExpect(jsonPath("$.status", is("ACTIVE")))
                    .andExpect(jsonPath("$.available", is(true)))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.lastModifiedAt", notNullValue()))
                    .andReturn();

            // Verify product was saved in database
            ProductResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ProductResponse.class
            );

            Product savedProduct = productRepository.findById(response.getId()).orElse(null);
            assertThat(savedProduct).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo(validRequest.getName());
            assertThat(savedProduct.getPrice().getAmount()).isEqualTo(BigDecimal.valueOf(validRequest.getPrice()));
        }

        @Test
        @DisplayName("Should successfully create multiple products sequentially")
        void shouldCreateMultipleProductsSuccessfully() throws Exception {
            // First product
            CreateProductRequest firstProduct = validRequest;

            // Second product with different category
            UUID secondCategoryId = UUID.randomUUID();
            CreateProductRequest secondProduct = CreateProductRequest.builder()
                    .name("Samsung Galaxy S24")
                    .brand("Samsung")
                    .shortDescription("Premium Android smartphone")
                    .description("Galaxy S24 with AI features and amazing display")
                    .keywords("samsung, galaxy, android, smartphone")
                    .price(899.99)
                    .count(30)
                    .discount(5.0)
                    .categoryId(secondCategoryId)
                    .build();

            MvcResult firstResult = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstProduct)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is(firstProduct.getName())))
                    .andReturn();

            MvcResult secondResult = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondProduct)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is(secondProduct.getName())))
                    .andReturn();

            // Extract IDs from responses
            ProductResponse firstResponse = objectMapper.readValue(
                    firstResult.getResponse().getContentAsString(),
                    ProductResponse.class
            );

            ProductResponse secondResponse = objectMapper.readValue(
                    secondResult.getResponse().getContentAsString(),
                    ProductResponse.class
            );

            // Verify first product exists in database by ID
            Product savedFirstProduct = productRepository.findById(firstResponse.getId()).orElse(null);
            assertThat(savedFirstProduct).isNotNull();
            assertThat(savedFirstProduct.getName()).isEqualTo(firstProduct.getName());
            assertThat(savedFirstProduct.getCategoryId()).isEqualTo(firstProduct.getCategoryId());

            // Verify second product exists in database by ID
            Product savedSecondProduct = productRepository.findById(secondResponse.getId()).orElse(null);
            assertThat(savedSecondProduct).isNotNull();
            assertThat(savedSecondProduct.getName()).isEqualTo(secondProduct.getName());
            assertThat(savedSecondProduct.getCategoryId()).isEqualTo(secondProduct.getCategoryId());
        }

        @Test
        @DisplayName("Should create product with only required fields")
        void shouldCreateProductWithOnlyRequiredFields() throws Exception {
            CreateProductRequest minimalRequest = CreateProductRequest.builder()
                    .name("Minimal Product")
                    .price(199.99)
                    .count(1)
                    .build();

            String requestBody = objectMapper.writeValueAsString(minimalRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is(minimalRequest.getName())))
                    .andExpect(jsonPath("$.brand", nullValue()))
                    .andExpect(jsonPath("$.shortDescription", nullValue()))
                    .andExpect(jsonPath("$.description", nullValue()))
                    .andExpect(jsonPath("$.keywords", nullValue()))
                    .andExpect(jsonPath("$.price", is(minimalRequest.getPrice())))
                    .andExpect(jsonPath("$.finalPrice", is(minimalRequest.getPrice())))
                    .andExpect(jsonPath("$.count",  is(minimalRequest.getCount())))
                    .andExpect(jsonPath("$.discount", is(0.0)))
                    .andExpect(jsonPath("$.categoryId", nullValue()))
                    .andExpect(jsonPath("$.status", is("ACTIVE")))
                    .andExpect(jsonPath("$.available", is(true)));
        }

        @Test
        @DisplayName("Should create product with zero discount")
        void shouldCreateProductWithZeroDiscount() throws Exception {
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("No Discount Product")
                    .price(500.0)
                    .count(1)
                    .discount(0.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.price", is(500.0)))
                    .andExpect(jsonPath("$.finalPrice", is(500.0)))
                    .andExpect(jsonPath("$.discount", is(0.0)))
                    .andExpect(jsonPath("$.count", is(1)));
        }
    }

    @Nested
    @DisplayName("Validation error scenarios")
    class CreateProductValidationTests {

        @Test
        @DisplayName("Should return 400 Bad Request when product name is missing")
        void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .brand("Apple")
                    .price(999.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when name is empty")
        void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("")
                    .brand("Apple")
                    .price(999.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when name is too short")
        void shouldReturnBadRequestWhenNameIsTooShort() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("IP") // Less than 3 characters
                    .brand("Apple")
                    .price(999.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when name is too long")
        void shouldReturnBadRequestWhenNameIsTooLong() throws Exception {
            String longName = "A".repeat(256); // More than 255 characters
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name(longName)
                    .brand("Apple")
                    .price(999.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is missing")
        void shouldReturnBadRequestWhenPriceIsMissing() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is zero")
        void shouldReturnBadRequestWhenPriceIsZero() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(0.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is negative")
        void shouldReturnBadRequestWhenPriceIsNegative() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(-100.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price exceeds maximum")
        void shouldReturnBadRequestWhenPriceExceedsMaximum() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(10_000_000.0) // Exceeds max
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when discount is negative")
        void shouldReturnBadRequestWhenDiscountIsNegative() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .discount(-10.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when discount exceeds 100%")
        void shouldReturnBadRequestWhenDiscountExceeds100() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .discount(150.0) // Exceeds 100%
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when short description exceeds max length")
        void shouldReturnBadRequestWhenShortDescriptionTooLong() throws Exception {
            String longDescription = "a".repeat(501); // Exceeds 500 characters

            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .shortDescription(longDescription)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when description exceeds max length")
        void shouldReturnBadRequestWhenDescriptionTooLong() throws Exception {
            String veryLongDescription = "a".repeat(2001); // Exceeds 2000 characters

            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .description(veryLongDescription)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when count is negative")
        void shouldReturnBadRequestWhenCountIsNegative() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .count(-5)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when count exceeds maximum")
        void shouldReturnBadRequestWhenCountExceedsMaximum() throws Exception {
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .count(1_000_000) // Exceeds max
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request for malformed JSON")
        void shouldReturnBadRequestForMalformedJson() throws Exception {
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{malformed json}"))
                    .andExpect(status().isBadRequest());
        }
    }

}