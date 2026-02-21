package org.rus.product.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rus.product.domain.Money;
import org.rus.product.domain.Product;
import org.rus.product.dto.ProductResponse;
import org.rus.product.dto.ProductSummaryResponse;
import org.rus.product.enums.Status;
import org.rus.product.infrastructure.model.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductMapper tests")
class ProductMapperTest {

    private static final Currency RUB = Currency.getInstance("RUB");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());

    private Product product;
    private ProductEntity productEntity;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        Money price = Money.rub(1000.00);
        Money finalPrice = Money.rub(900.00); // со скидкой 10%

        product = Product.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .name("Test Product")
                .article("ART-001")
                .brand("Test Brand")
                .shortDescription("Short description")
                .description("Full description")
                .keywords("test,product")
                .price(price)
                .count(10)
                .discount(10.0)
                .categoryId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .creatorId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .status(Status.ACTIVE)
                .createdAt(now)
                .lastModifiedAt(now)
                .deletedAt(null)
                .build();

        productEntity = new ProductEntity();
        productEntity.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        productEntity.setName("Test Product");
        productEntity.setBrand("Test Brand");
        productEntity.setShortDescription("Short description");
        productEntity.setDescription("Full description");
        productEntity.setKeywords("test,product");
        productEntity.setPriceAmount(new BigDecimal("1000.00"));
        productEntity.setPriceCurrency("RUB");
        productEntity.setCount(10);
        productEntity.setDiscount(10.0);
        productEntity.setCategoryId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        productEntity.setCreatorId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        productEntity.setStatus(Status.ACTIVE);
        productEntity.setCreatedAt(now);
        productEntity.setLastModifiedAt(now);
        productEntity.setDeletedAt(null);
    }

    @Nested
    @DisplayName("toResponse method tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should map Product to ProductResponse correctly")
        void shouldMapProductToProductResponse() {
            // When
            ProductResponse response = ProductMapper.toResponse(product);

            // Then
            assertThat(response.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(response.getName()).isEqualTo("Test Product");
            assertThat(response.getBrand()).isEqualTo("Test Brand");
            assertThat(response.getShortDescription()).isEqualTo("Short description");
            assertThat(response.getDescription()).isEqualTo("Full description");
            assertThat(response.getKeywords()).isEqualTo("test,product");
            assertThat(response.getPrice()).isEqualByComparingTo("1000.00");
            assertThat(response.getCurrency()).isEqualTo("RUB");
            assertThat(response.getFinalPrice()).isEqualByComparingTo("900.00");
            assertThat(response.getCount()).isEqualTo(10);
            assertThat(response.getDiscount()).isEqualTo(10.0);
            assertThat(response.getCategoryId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(response.getCreatorId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            assertThat(response.isAvailable()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(DATE_FORMATTER.format(now));
            assertThat(response.getLastModifiedAt()).isEqualTo(DATE_FORMATTER.format(now));
        }

        @Test
        @DisplayName("Should return null when product is null")
        void shouldReturnNullWhenProductIsNull() {
            // When
            ProductResponse response = ProductMapper.toResponse(null);

            // Then
            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should handle null price fields")
        void shouldHandleNullPriceFields() {
            // Given
            Product productWithNullPrice = Product.builder()
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .name("Product Without Price")
                    .createdAt(now)
                    .lastModifiedAt(now)
                    .build();

            // When
            ProductResponse response = ProductMapper.toResponse(productWithNullPrice);

            // Then
            assertThat(response.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(response.getName()).isEqualTo("Product Without Price");
            assertThat(response.getPrice()).isNull();
            assertThat(response.getCurrency()).isNull();
            assertThat(response.getFinalPrice()).isNull();
        }

        @Test
        @DisplayName("Should handle null status")
        void shouldHandleNullStatus() {
            // Given
            Product productWithNullStatus = Product.builder()
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .name("Product Without Status")
                    .price(Money.rub(500.00))
                    .createdAt(now)
                    .lastModifiedAt(now)
                    .status(null)
                    .build();

            // When
            ProductResponse response = ProductMapper.toResponse(productWithNullStatus);

            // Then
            assertThat(response.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("toSummaryResponse method tests")
    class ToSummaryResponseTests {

        @Test
        @DisplayName("Should map Product to ProductSummaryResponse correctly")
        void shouldMapProductToProductSummaryResponse() {
            // When
            ProductSummaryResponse response = ProductMapper.toSummaryResponse(product);

            // Then
            assertThat(response.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(response.getName()).isEqualTo("Test Product");
            assertThat(response.getBrand()).isEqualTo("Test Brand");
            assertThat(response.getFinalPrice()).isEqualByComparingTo("900.00");
            assertThat(response.getCurrency()).isEqualTo("RUB");
            assertThat(response.isAvailable()).isTrue();
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Should return null when product is null")
        void shouldReturnNullWhenProductIsNull() {
            // When
            ProductSummaryResponse response = ProductMapper.toSummaryResponse(null);

            // Then
            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should handle null price and currency in summary response")
        void shouldHandleNullPriceAndCurrency() {
            // Given
            Product productWithNullPrice = Product.builder()
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .name("Product Without Price")
                    .build();

            // When
            ProductSummaryResponse response = ProductMapper.toSummaryResponse(productWithNullPrice);

            // Then
            assertThat(response.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(response.getName()).isEqualTo("Product Without Price");
            assertThat(response.getFinalPrice()).isNull();
            assertThat(response.getCurrency()).isNull();
        }

        @Test
        @DisplayName("Should handle null status in summary response")
        void shouldHandleNullStatus() {
            // Given
            Product productWithNullStatus = Product.builder()
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .name("Product Without Status")
                    .price(Money.rub(500.00))
                    .status(null)
                    .build();

            // When
            ProductSummaryResponse response = ProductMapper.toSummaryResponse(productWithNullStatus);

            // Then
            assertThat(response.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("fromDomain method tests")
    class FromDomainTests {

        @Test
        @DisplayName("Should map Product to ProductEntity correctly")
        void shouldMapProductToProductEntity() {
            // When
            ProductEntity entity = ProductMapper.fromDomain(product);

            // Then
            assertThat(entity.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(entity.getName()).isEqualTo("Test Product");
            assertThat(entity.getBrand()).isEqualTo("Test Brand");
            assertThat(entity.getShortDescription()).isEqualTo("Short description");
            assertThat(entity.getDescription()).isEqualTo("Full description");
            assertThat(entity.getKeywords()).isEqualTo("test,product");
            assertThat(entity.getPriceAmount()).isEqualByComparingTo("1000.00");
            assertThat(entity.getPriceCurrency()).isEqualTo("RUB");
            assertThat(entity.getCount()).isEqualTo(10);
            assertThat(entity.getDiscount()).isEqualTo(10.0);
            assertThat(entity.getCategoryId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(entity.getCreatorId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(entity.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getLastModifiedAt()).isEqualTo(now);
            assertThat(entity.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null price fields when mapping to entity")
        void shouldHandleNullPriceFields() {
            // Given
            Product productWithoutPrice = Product.builder()
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .name("Product Without Price")
                    .price(null)
                    .createdAt(now)
                    .lastModifiedAt(now)
                    .build();

            // When
            ProductEntity entity = ProductMapper.fromDomain(productWithoutPrice);

            // Then
            assertThat(entity.getPriceAmount()).isNull();
            assertThat(entity.getPriceCurrency()).isNull();
        }

        @Test
        @DisplayName("Should handle null timestamps")
        void shouldHandleNullTimestamps() {
            // Given
            Product productWithNullTimestamps = Product.builder()
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .name("Product With Null Timestamps")
                    .price(Money.rub(500.00))
                    .createdAt(null)
                    .lastModifiedAt(null)
                    .deletedAt(null)
                    .build();

            // When
            ProductEntity entity = ProductMapper.fromDomain(productWithNullTimestamps);

            // Then
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getLastModifiedAt()).isNull();
            assertThat(entity.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain method tests")
    class ToDomainTests {

        @Test
        @DisplayName("Should map ProductEntity to Product correctly")
        void shouldMapProductEntityToProduct() {
            // When
            Product result = ProductMapper.toDomain(productEntity);

            // Then
            assertThat(result.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getBrand()).isEqualTo("Test Brand");
            assertThat(result.getShortDescription()).isEqualTo("Short description");
            assertThat(result.getDescription()).isEqualTo("Full description");
            assertThat(result.getKeywords()).isEqualTo("test,product");
            assertThat(result.getPrice().getAmount()).isEqualByComparingTo("1000.00");
            assertThat(result.getPrice().getCurrency()).isEqualTo(RUB);
            assertThat(result.getCount()).isEqualTo(10);
            assertThat(result.getDiscount()).isEqualTo(10.0);
            assertThat(result.getCategoryId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(result.getCreatorId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getLastModifiedAt()).isEqualTo(now);
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when price fields are inconsistent")
        void shouldThrowExceptionWhenPriceFieldsInconsistent() {
            // Given
            productEntity.setPriceAmount(new BigDecimal("1000.00"));
            productEntity.setPriceCurrency(null);

            // When & Then
            assertThatThrownBy(() -> ProductMapper.toDomain(productEntity))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle null timestamps")
        void shouldHandleNullTimestamps() {
            // Given
            productEntity.setCreatedAt(null);
            productEntity.setLastModifiedAt(null);
            productEntity.setDeletedAt(null);

            // When
            Product result = ProductMapper.toDomain(productEntity);

            // Then
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getLastModifiedAt()).isNull();
            assertThat(result.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Bidirectional mapping tests")
    class BidirectionalMappingTests {

        @Test
        @DisplayName("Should maintain data integrity when mapping domain -> entity -> domain")
        void shouldMaintainDataIntegrityInRoundTrip() {
            // Given
            Product originalProduct = product;

            // When
            ProductEntity entity = ProductMapper.fromDomain(originalProduct);
            Product resultProduct = ProductMapper.toDomain(entity);

            // Then
            assertThat(resultProduct.getId()).isEqualTo(originalProduct.getId());
            assertThat(resultProduct.getName()).isEqualTo(originalProduct.getName());
            assertThat(resultProduct.getBrand()).isEqualTo(originalProduct.getBrand());
            assertThat(resultProduct.getShortDescription()).isEqualTo(originalProduct.getShortDescription());
            assertThat(resultProduct.getDescription()).isEqualTo(originalProduct.getDescription());
            assertThat(resultProduct.getKeywords()).isEqualTo(originalProduct.getKeywords());

            assertThat(resultProduct.getPrice().getAmount())
                    .isEqualByComparingTo(originalProduct.getPrice().getAmount());
            assertThat(resultProduct.getPrice().getCurrency())
                    .isEqualTo(originalProduct.getPrice().getCurrency());

            assertThat(resultProduct.getCount()).isEqualTo(originalProduct.getCount());
            assertThat(resultProduct.getDiscount()).isEqualTo(originalProduct.getDiscount());
            assertThat(resultProduct.getCategoryId()).isEqualTo(originalProduct.getCategoryId());
            assertThat(resultProduct.getCreatorId()).isEqualTo(originalProduct.getCreatorId());
            assertThat(resultProduct.getStatus()).isEqualTo(originalProduct.getStatus());
            assertThat(resultProduct.getCreatedAt()).isEqualTo(originalProduct.getCreatedAt());
            assertThat(resultProduct.getLastModifiedAt()).isEqualTo(originalProduct.getLastModifiedAt());
            assertThat(resultProduct.getDeletedAt()).isEqualTo(originalProduct.getDeletedAt());
        }

        @Test
        @DisplayName("Should handle null values in round trip mapping")
        void shouldHandleNullValuesInRoundTrip() {
            // Given
            Product productWithNulls = Product.builder()
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                    .name("Product With Nulls")
                    .price(Money.rub(2))
                    .status(null)
                    .createdAt(now)
                    .lastModifiedAt(now)
                    .build();

            // When
            ProductEntity entity = ProductMapper.fromDomain(productWithNulls);
            Product result = ProductMapper.toDomain(entity);

            // Then
            assertThat(result.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            assertThat(result.getName()).isEqualTo("Product With Nulls");
            assertThat(result.getPrice()).isEqualTo(Money.rub(2));
            assertThat(result.getStatus()).isNull();
        }
    }

}