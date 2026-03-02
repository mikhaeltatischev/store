package org.rus.product.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.rus.product.enums.Status;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Model: Product")
class ProductTest {

    private UUID productId;
    private UUID creatorId;
    private UUID categoryId;
    private Money basePrice;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        basePrice = Money.rub(10000.00);

        product = Product.builder()
                .id(productId)
                .name("Test Product")
                .article("TEST-001")
                .brand("TestBrand")
                .shortDescription("Short description")
                .description("Full description")
                .keywords("test, product")
                .price(basePrice)
                .status(Status.ACTIVE)
                .count(10)
                .discount(10.0)
                .categoryId(categoryId)
                .creatorId(creatorId)
                .build();
    }

    @Nested
    @DisplayName("Product Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create product with valid data")
        void shouldCreateProductWithValidData() {
            assertNotNull(product);
            assertEquals(productId, product.getId());
            assertEquals("Test Product", product.getName());
            assertEquals("TEST-001", product.getArticle());
            assertEquals(basePrice, product.getPrice());
            assertEquals(Integer.valueOf(10), product.getCount());
            assertEquals(Double.valueOf(10.0), product.getDiscount());
            assertEquals(categoryId, product.getCategoryId());
            assertEquals(creatorId, product.getCreatorId());
            assertEquals(Status.ACTIVE, product.getStatus());
        }

    }

    @Nested
    @DisplayName("Product Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should calculate final price correctly with discount")
        void shouldCalculateFinalPriceWithDiscount() {
            Money finalPrice = product.getFinalPrice();

            // 10000 - 10% = 9000
            assertEquals(0, finalPrice.getAmount().compareTo(BigDecimal.valueOf(9000.00)));
            assertEquals(Currency.getInstance("RUB"), finalPrice.getCurrency());
        }

        @Test
        @DisplayName("Should return original price when discount is null")
        void shouldReturnOriginalPriceWhenDiscountIsNull() {
            Product noDiscountProduct = Product.builder()
                    .id(productId)
                    .name("No Discount Product")
                    .article("ND-001")
                    .price(basePrice)
                    .creatorId(creatorId)
                    .build();

            Money finalPrice = noDiscountProduct.getFinalPrice();
            assertEquals(basePrice, finalPrice);
        }

        @Test
        @DisplayName("Should correctly identify non-creator")
        void shouldCorrectlyIdentifyNonCreator() {
            UUID otherUserId = UUID.randomUUID();

            assertFalse(product.checkCreator(creatorId));
            assertTrue(product.checkCreator(otherUserId));
            assertTrue(product.checkCreator(null));
        }

        @Test
        @DisplayName("Should correctly check product availability")
        void shouldCorrectlyCheckProductAvailability() {
            assertTrue(product.isAvailable());

            Product outOfStockProduct = Product.builder()
                    .id(productId)
                    .name("Out of Stock")
                    .article("OOS-001")
                    .price(basePrice)
                    .count(0)
                    .creatorId(creatorId)
                    .build();
            assertFalse(outOfStockProduct.isAvailable());

            Product noCountProduct = Product.builder()
                    .id(productId)
                    .name("No Count")
                    .article("NC-001")
                    .price(basePrice)
                    .creatorId(creatorId)
                    .build();
            assertFalse(noCountProduct.isAvailable());
        }
    }

    @Nested
    @DisplayName("Product Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should decrease stock correctly")
        void shouldDecreaseStockCorrectly() {
            int initialCount = product.getCount();
            int decreaseQuantity = 3;

            product.decreaseStock(decreaseQuantity);

            assertEquals(initialCount - decreaseQuantity, product.getCount());
            assertNotNull(product.getLastModifiedAt());
        }

        @Test
        @DisplayName("Should change status to OUT_OF_STOCK when count becomes zero")
        void shouldChangeStatusToOutOfStockWhenCountBecomesZero() {
            product.decreaseStock(10);

            assertEquals(Status.OUT_OF_STOCK, product.getStatus());
            assertEquals(Integer.valueOf(0), product.getCount());
        }

        @Test
        @DisplayName("Should throw exception when decreasing more than available")
        void shouldThrowExceptionWhenDecreasingMoreThanAvailable() {
            Executable executable = () -> product.decreaseStock(20);

            assertThrows(IllegalStateException.class, executable);
        }

        @Test
        @DisplayName("Should throw exception when decreasing with negative quantity")
        void shouldThrowExceptionWhenDecreasingWithNegativeQuantity() {
            Executable executable = () -> product.decreaseStock(-5);

            assertThrows(IllegalArgumentException.class, executable);
        }

        @Test
        @DisplayName("Should increase stock correctly")
        void shouldIncreaseStockCorrectly() {
            int initialCount = product.getCount();
            int increaseQuantity = 5;

            product.increaseStock(increaseQuantity);

            assertEquals(initialCount + increaseQuantity, product.getCount());
            assertNotNull(product.getLastModifiedAt());
        }

        @Test
        @DisplayName("Should change status back to ACTIVE when stock increased from zero")
        void shouldChangeStatusToActiveWhenStockIncreasedFromZero() {
            product.decreaseStock(10);
            assertEquals(Status.OUT_OF_STOCK, product.getStatus());

            product.increaseStock(5);

            assertEquals(Status.ACTIVE, product.getStatus());
            assertEquals(Integer.valueOf(5), product.getCount());
        }
    }

    @Nested
    @DisplayName("Product Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update details correctly when editor is creator")
        void shouldUpdateDetailsCorrectlyWhenEditorIsCreator() {
            String newName = "Updated Product Name";
            String newBrand = "UpdatedBrand";
            String newShortDesc = "Updated short description";
            Double newDiscount = 15.0;
            UUID newCategoryId = UUID.randomUUID();

            product.updateDetails(
                    newName, newBrand, newShortDesc, null, null,
                    newDiscount, newCategoryId, creatorId, null, null
            );

            assertEquals(newName, product.getName());
            assertEquals(newBrand, product.getBrand());
            assertEquals(newShortDesc, product.getShortDescription());
            assertEquals(newDiscount, product.getDiscount());
            assertEquals(newCategoryId, product.getCategoryId());
            assertEquals("Full description", product.getDescription());
            assertEquals("test, product", product.getKeywords());
            assertNotNull(product.getLastModifiedAt());
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            String newName = "Updated Product Name";
            Double newDiscount = 15.0;

            product.updateDetails(
                    newName, null, null, null, null,
                    newDiscount, null, creatorId, null, null
            );

            assertEquals(newName, product.getName());
            assertEquals(newDiscount, product.getDiscount());
            assertEquals("TestBrand", product.getBrand());
            assertEquals("Short description", product.getShortDescription());
            assertEquals(categoryId, product.getCategoryId());
        }

//        @Test
//        @DisplayName("Should throw exception when updating details with non-creator")
//        void shouldThrowExceptionWhenUpdatingDetailsWithNonCreator() {
//            UUID nonCreatorId = UUID.randomUUID();
//
//            Executable executable = () -> product.updateDetails(
//                    "New Name", null, null, null, null,
//                    null, null, nonCreatorId, null, null
//            );
//
//            assertThrows(SecurityException.class, executable);
//        }

        @Test
        @DisplayName("Should throw exception when updating details with invalid discount")
        void shouldThrowExceptionWhenUpdatingDetailsWithInvalidDiscount() {
            Executable executable = () -> product.updateDetails(
                    null, null, null, null, null,
                    150.0, null, creatorId, null, null
            );

            assertThrows(IllegalArgumentException.class, executable);
        }

        @Test
        @DisplayName("Should mark product as deleted")
        void shouldMarkProductAsDeleted() {
            product.markAsDeleted();

            assertEquals(Status.DELETED, product.getStatus());
            assertNotNull(product.getDeletedAt());
            assertNotNull(product.getLastModifiedAt());
        }
    }

}