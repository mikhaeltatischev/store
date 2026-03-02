package org.rus.product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rus.product.domain.Money;
import org.rus.product.domain.Product;
import org.rus.product.dto.PageRequest;
import org.rus.product.dto.PageResponse;
import org.rus.product.exception.ProductNotFoundException;
import org.rus.product.infrastructure.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private UUID categoryId;
    private UUID creatorId;
    private UUID editorId;
    private Product testProduct;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        editorId = UUID.randomUUID();

        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .brand("Test Brand")
                .shortDescription("Short description")
                .description("Full description")
                .keywords("test,product")
                .price(Money.rub(1000.0))
                .count(10)
                .discount(0.0)
                .categoryId(categoryId)
                .creatorId(creatorId)
                .build();

        pageRequest = new PageRequest(0, 10);
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            String name = "New Product";
            String brand = "New Brand";
            String shortDesc = "Short desc";
            String desc = "Full desc";
            String keywords = "new,product";
            double price = 500.0;
            Integer count = 5;
            Double discount = 10.0;

            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Product result = productService.createProduct(
                    name, brand, shortDesc, desc, keywords,
                    price, count, discount, categoryId, creatorId
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getBrand()).isEqualTo(brand);
            assertThat(result.getShortDescription()).isEqualTo(shortDesc);
            assertThat(result.getDescription()).isEqualTo(desc);
            assertThat(result.getKeywords()).isEqualTo(keywords);
            assertThat(result.getCount()).isEqualTo(count);
            assertThat(result.getDiscount()).isEqualTo(discount);
            assertThat(result.getCategoryId()).isEqualTo(categoryId);
            assertThat(result.getCreatorId()).isEqualTo(creatorId);

            BigDecimal expectedPrice = BigDecimal.valueOf(500)
                    .subtract(BigDecimal.valueOf(500)
                            .multiply(BigDecimal.valueOf(0.1)));

            assertThat(result.getFinalPrice().getAmount()).isEqualByComparingTo(expectedPrice);

            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should create product with null discount")
        void shouldCreateProductWithNullDiscount() {
            // Given
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Product result = productService.createProduct(
                    "Product", "Brand", "Short", "Desc", "keywords",
                    100.0, 5, null, categoryId, creatorId
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDiscount()).isEqualTo(0.0);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            // When
            Product result = productService.getProduct(productId);

            // Then
            assertThat(result).isEqualTo(testProduct);
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.getProduct(productId))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("Product not found with id: " + productId);

            verify(productRepository).findById(productId);
        }
    }

    @Nested
    @DisplayName("Update Product Details Tests")
    class UpdateProductDetailsTests {

        @Test
        @DisplayName("Should update product details successfully")
        void shouldUpdateProductDetailsSuccessfully() {
            // Given
            String newName = "Updated Name";
            String newBrand = "Updated Brand";
            String newShortDesc = "Updated Short Desc";
            String newDesc = "Updated Desc";
            String newKeywords = "updated,keywords";
            Double price = 270.0;
            Double newDiscount = 15.0;
            Integer count = 2;
            UUID newCategoryId = UUID.randomUUID();

            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Product result = productService.updateProductDetails(
                    productId, newName, newBrand, newShortDesc, newDesc,
                    newKeywords, newDiscount, newCategoryId, creatorId, price, count
            );

            // Then
            assertThat(result.getName()).isEqualTo(newName);
            assertThat(result.getBrand()).isEqualTo(newBrand);
            assertThat(result.getShortDescription()).isEqualTo(newShortDesc);
            assertThat(result.getDescription()).isEqualTo(newDesc);
            assertThat(result.getKeywords()).isEqualTo(newKeywords);
            assertThat(result.getDiscount()).isEqualTo(newDiscount);
            assertThat(result.getCategoryId()).isEqualTo(newCategoryId);
            assertThat(result.getPrice().getAmount().doubleValue()).isEqualTo(price);
            assertThat(result.getCount()).isEqualTo(count);

            verify(productRepository).findById(productId);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should update product details with null values")
        void shouldUpdateProductDetailsWithNullValues() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Product result = productService.updateProductDetails(
                    productId, null, null, null, null,
                    null, null, null, creatorId, null, null
            );

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).findById(productId);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should decrease stock successfully")
        void shouldDecreaseStockSuccessfully() {
            // Given
            int decreaseQuantity = 3;
            int initialCount = testProduct.getCount();
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Product result = productService.decreaseStock(productId, decreaseQuantity);

            // Then
            assertThat(result.getCount()).isEqualTo(initialCount - decreaseQuantity);
            verify(productRepository).findById(productId);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when decreasing stock below zero")
        void shouldThrowExceptionWhenDecreasingBelowZero() {
            // Given
            int decreaseQuantity = 20; // More than available
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

            // When/Then
            assertThatThrownBy(() -> productService.decreaseStock(productId, decreaseQuantity))
                    .isInstanceOf(IllegalStateException.class);

            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should increase stock successfully")
        void shouldIncreaseStockSuccessfully() {
            // Given
            int increaseQuantity = 5;
            int initialCount = testProduct.getCount();
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Product result = productService.increaseStock(productId, increaseQuantity);

            // Then
            assertThat(result.getCount()).isEqualTo(initialCount + increaseQuantity);
            verify(productRepository).findById(productId);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("Should get products by creator")
        void shouldGetProductsByCreator() {
            // Given
            PageResponse<Product> response = PageResponse.<Product>builder()
                    .content(List.of(testProduct))
                    .page(0)
                    .size(10)
                    .totalElements(100L)
                    .totalPages(10)
                    .first(true)
                    .last(false)
                    .empty(false)
                    .build();

            when(productRepository.findByCreatorId(creatorId, pageRequest))
                    .thenReturn(response);

            // When
            PageResponse<Product> result = productService.getProductsByCreator(creatorId, pageRequest);

            // Then
            assertThat(result).isEqualTo(response);
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).findByCreatorId(creatorId, pageRequest);
        }

        @Test
        @DisplayName("Should get available products")
        void shouldGetAvailableProducts() {
            // Given
            PageResponse<Product> response = PageResponse.<Product>builder()
                    .content(List.of(testProduct))
                    .page(0)
                    .size(10)
                    .totalElements(100L)
                    .totalPages(10)
                    .first(true)
                    .last(false)
                    .empty(false)
                    .build();

            when(productRepository.findAvailableProducts(pageRequest))
                    .thenReturn(response);

            // When
            PageResponse<Product> result = productService.getAvailableProducts(pageRequest);

            // Then
            assertThat(result).isEqualTo(response);
            verify(productRepository).findAvailableProducts(pageRequest);
        }

        @Test
        @DisplayName("Should get products by category")
        void shouldGetProductsByCategory() {
            // Given
            PageResponse<Product> response = PageResponse.<Product>builder()
                    .content(List.of(testProduct))
                    .page(0)
                    .size(10)
                    .totalElements(100L)
                    .totalPages(10)
                    .first(true)
                    .last(false)
                    .empty(false)
                    .build();

            when(productRepository.findByCategoryId(categoryId, pageRequest))
                    .thenReturn(response);

            // When
            PageResponse<Product> result = productService.getProductsByCategory(categoryId, pageRequest);

            // Then
            assertThat(result).isEqualTo(response);
            verify(productRepository).findByCategoryId(categoryId, pageRequest);
        }

        @Test
        @DisplayName("Should return empty page when no products found")
        void shouldReturnEmptyPageWhenNoProductsFound() {
            // Given
            PageResponse<Product> response = PageResponse.<Product>builder()
                    .content(List.of())
                    .page(0)
                    .size(10)
                    .totalElements(0L)
                    .totalPages(10)
                    .first(true)
                    .last(false)
                    .empty(false)
                    .build();

            when(productRepository.findByCreatorId(creatorId, pageRequest))
                    .thenReturn(response);

            // When
            PageResponse<Product> result = productService.getProductsByCreator(creatorId, pageRequest);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(productRepository).findByCreatorId(creatorId, pageRequest);
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            doNothing().when(productRepository).delete(testProduct);

            // When
            productService.deleteProduct(productId, editorId);

            // Then
            verify(productRepository).findById(productId);
            verify(productRepository).delete(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.deleteProduct(productId, editorId))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(productRepository).findById(productId);
            verify(productRepository, never()).delete(any());
        }
    }

    @Test
    @DisplayName("Should verify repository interactions")
    void shouldVerifyRepositoryInteractions() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        productService.getProduct(productId);

        // Then
        verify(productRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(productRepository);
    }

}