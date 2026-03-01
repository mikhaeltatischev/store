package org.rus.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rus.product.domain.Product;
import org.rus.product.dto.CreateProductRequest;
import org.rus.product.dto.ProductResponse;
import org.rus.product.infrastructure.repository.ProductRepository;
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

    @BeforeEach
    void setUp() {
        // Reset sequences if using PostgreSQL
        try {
            jdbcTemplate.execute("TRUNCATE TABLE products RESTART IDENTITY CASCADE");
        } catch (Exception e) {
            // Ignore if not PostgreSQL or table doesn't exist
        }
    }

    @Nested
    @DisplayName("POST /products - Create Product")
    class CreateProductTests {

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

        @Test
        @DisplayName("Should successfully create a product with valid data")
        void shouldCreateProductSuccessfully() throws Exception {
            // Given
            String requestBody = objectMapper.writeValueAsString(validRequest);

            // When & Then
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
                    .andExpect(jsonPath("$.status", is("CREATED")))
                    .andExpect(jsonPath("$.available", is(false)))
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
            // Given - First product
            CreateProductRequest firstProduct = validRequest;

            // Given - Second product with different category
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

            // When & Then - Create first product and capture its ID
            MvcResult firstResult = mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstProduct)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is(firstProduct.getName())))
                    .andReturn();

            // When & Then - Create second product and capture its ID
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
            String name = "Minimal Product";
            String shortDescription = "Short description";
            String fullDescription = "Full description";
            Double price = 199.99;
            int count = 1;

            // Given
            CreateProductRequest minimalRequest = CreateProductRequest.builder()
                    .name(name)
                    .shortDescription(shortDescription)
                    .description(fullDescription)
                    .price(price)
                    .count(count)
                    .build();

            String requestBody = objectMapper.writeValueAsString(minimalRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is(minimalRequest.getName())))
                    .andExpect(jsonPath("$.brand", nullValue()))
                    .andExpect(jsonPath("$.shortDescription", is(minimalRequest.getShortDescription())))
                    .andExpect(jsonPath("$.description", is(minimalRequest.getDescription())))
                    .andExpect(jsonPath("$.keywords", nullValue()))
                    .andExpect(jsonPath("$.price", is(minimalRequest.getPrice())))
                    .andExpect(jsonPath("$.finalPrice", is(minimalRequest.getPrice()))) // No discount
                    .andExpect(jsonPath("$.count", is(minimalRequest.getCount()))) // Default count
                    .andExpect(jsonPath("$.discount", is(0.0))) // Default discount
                    .andExpect(jsonPath("$.categoryId", nullValue()))
                    .andExpect(jsonPath("$.status", is("CREATED"))) // Default status after create
                    .andExpect(jsonPath("$.available", is(false))); // count = 1, status = created - so not available
        }

        @Test
        @DisplayName("Should return 400 Bad Request when product name is missing")
        void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
            // Given
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .brand("Apple")
                    .price(999.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when name is too short")
        void shouldReturnBadRequestWhenNameIsTooShort() throws Exception {
            // Given
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("IP") // Less than 3 characters
                    .brand("Apple")
                    .price(999.99)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is missing")
        void shouldReturnBadRequestWhenPriceIsMissing() throws Exception {
            // Given
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price is zero")
        void shouldReturnBadRequestWhenPriceIsZero() throws Exception {
            // Given
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(0.0)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when price exceeds maximum")
        void shouldReturnBadRequestWhenPriceExceedsMaximum() throws Exception {
            // Given
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(10_000_000.0) // Exceeds max
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when discount exceeds 100%")
        void shouldReturnBadRequestWhenDiscountExceeds100() throws Exception {
            // Given
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .discount(150.0) // Exceeds 100%
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when description exceeds max length")
        void shouldReturnBadRequestWhenDescriptionTooLong() throws Exception {
            // Given
            String veryLongDescription = "a".repeat(2001); // Exceeds 2000 characters

            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .description(veryLongDescription)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when count is negative")
        void shouldReturnBadRequestWhenCountIsNegative() throws Exception {
            // Given
            CreateProductRequest invalidRequest = CreateProductRequest.builder()
                    .name("iPhone 15 Pro")
                    .brand("Apple")
                    .price(999.99)
                    .count(-5)
                    .build();

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /products - Get Product by ID")
    class GetProductTests {

        private UUID existingProductId;
        private CreateProductRequest productRequest;

        @BeforeEach
        void setUp() throws Exception {
            // Create a product to use in tests
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

        @Test
        @DisplayName("Should successfully retrieve product by ID")
        void shouldGetProductByIdSuccessfully() throws Exception {
            // When & Then
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
                    .andExpect(jsonPath("$.finalPrice", is(424.99))) // 499.99 - 15% = 424.9915 -> rounded to 424.99
                    .andExpect(jsonPath("$.count", is(productRequest.getCount())))
                    .andExpect(jsonPath("$.discount", is(productRequest.getDiscount())))
                    .andExpect(jsonPath("$.categoryId", is(productRequest.getCategoryId().toString())))
                    .andExpect(jsonPath("$.creatorId", notNullValue()))
                    .andExpect(jsonPath("$.status", is("CREATED")))
                    .andExpect(jsonPath("$.available", is(false)))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.lastModifiedAt", notNullValue()));
        }

        @Test
        @DisplayName("Should return 404 Not Found when product does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(get("/products")
                            .param("id", nonExistentId.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID parameter is missing")
        void shouldReturn400WhenIdParameterMissing() throws Exception {
            // When & Then
            mockMvc.perform(get("/products"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID is invalid UUID format")
        void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
            // When & Then
            mockMvc.perform(get("/products")
                            .param("id", "invalid-uuid-format"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should retrieve multiple different products by their IDs")
        void shouldRetrieveMultipleProductsById() throws Exception {
            // Given - Create a second product
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

            // When & Then - Retrieve first product
            mockMvc.perform(get("/products")
                            .param("id", existingProductId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingProductId.toString())))
                    .andExpect(jsonPath("$.name", is(productRequest.getName())));

            // When & Then - Retrieve second product
            mockMvc.perform(get("/products")
                            .param("id", secondCreatedProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(secondCreatedProduct.getId().toString())))
                    .andExpect(jsonPath("$.name", is(secondProductRequest.getName())));
        }

        @Test
        @DisplayName("Should return product with correct available status when count is zero")
        void shouldReturnProductWithAvailableFalseWhenCountZero() throws Exception {
            // Given - Create a product with zero count
            CreateProductRequest zeroCountRequest = CreateProductRequest.builder()
                    .name("Out of Stock Product")
                    .brand("TestBrand")
                    .price(99.99)
                    .count(0)  // Zero count
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

            // When & Then
            mockMvc.perform(get("/products")
                            .param("id", zeroCountProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(zeroCountProduct.getId().toString())))
                    .andExpect(jsonPath("$.name", is(zeroCountRequest.getName())))
                    .andExpect(jsonPath("$.count", is(0)))
                    .andExpect(jsonPath("$.available", is(false))); // Should be false when count is 0
        }

        @Test
        @DisplayName("Should return product with correct final price calculation when discount applied")
        void shouldReturnProductWithCorrectFinalPrice() throws Exception {
            // Given - Create product with various discount scenarios
            CreateProductRequest discountProductRequest = CreateProductRequest.builder()
                    .name("Discounted Product")
                    .brand("TestBrand")
                    .price(1000.00)
                    .count(5)
                    .discount(25.5)  // 25.5% discount
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

            // Expected final price: 1000.00 - (1000.00 * 25.5 / 100) = 745.00
            BigDecimal expectedFinalPrice = new BigDecimal("745.00");

            // When & Then
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
            // Given - Create product with minimal fields
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

            // When & Then
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

}