package org.rus.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money class tests")
class MoneyTest {

    private static final Currency RUB = Currency.getInstance("RUB");
    private static final Currency USD = Currency.getInstance("USD");

    @Nested
    @DisplayName("Constructor and factory methods tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create Money with BigDecimal amount and currency")
        void shouldCreateMoneyWithBigDecimalAndCurrency() {
            BigDecimal amount = new BigDecimal("100.50");

            Money money = Money.of(amount, USD);

            assertThat(money.getAmount()).isEqualByComparingTo("100.50");
            assertThat(money.getCurrency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should create Money with double amount and currency")
        void shouldCreateMoneyWithDoubleAndCurrency() {
            double amount = 100.50;

            Money money = Money.of(amount, USD);

            assertThat(money.getAmount()).isEqualByComparingTo("100.50");
            assertThat(money.getCurrency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should create Rub Money with BigDecimal amount")
        void shouldCreateRubMoneyWithBigDecimal() {
            BigDecimal amount = new BigDecimal("500.75");

            Money money = Money.rub(amount);

            assertThat(money.getAmount()).isEqualByComparingTo("500.75");
            assertThat(money.getCurrency()).isEqualTo(RUB);
        }

        @Test
        @DisplayName("Should create Rub Money with double amount")
        void shouldCreateRubMoneyWithDouble() {
            double amount = 500.75;

            Money money = Money.rub(amount);

            assertThat(money.getAmount()).isEqualByComparingTo("500.75");
            assertThat(money.getCurrency()).isEqualTo(RUB);
        }

        @Test
        @DisplayName("Should preserve scale of 2 for integer amounts")
        void shouldPreserveScaleOf2ForIntegerAmounts() {
            Money money = Money.rub(100);

            assertThat(money.getAmount()).isEqualByComparingTo("100.00");
            assertThat(money.getAmount().scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Multiply method tests")
    class MultiplyTests {

        @Test
        @DisplayName("Should multiply amount by multiplier")
        void shouldMultiplyAmountByMultiplier() {
            Money money = Money.rub(100.00);
            BigDecimal multiplier = new BigDecimal("1.5");

            Money result = money.multiply(multiplier);

            assertThat(result.getAmount()).isEqualByComparingTo("150.00");
            assertThat(result.getCurrency()).isEqualTo(RUB);
        }

        @ParameterizedTest
        @CsvSource({
                "100.00, 2, 200.00",
                "100.00, 0.5, 50.00",
                "100.00, 0.33, 33.00",
                "100.00, 0.333, 33.30"
        })
        @DisplayName("Should multiply various amounts correctly")
        void shouldMultiplyVariousAmountsCorrectly(String amount, String multiplier, String expected) {
            Money money = Money.rub(new BigDecimal(amount));

            Money result = money.multiply(new BigDecimal(multiplier));

            assertThat(result.getAmount()).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("Should preserve currency after multiplication")
        void shouldPreserveCurrencyAfterMultiplication() {
            Money money = Money.of(100.00, USD);

            Money result = money.multiply(new BigDecimal("2"));

            assertThat(result.getCurrency()).isEqualTo(USD);
        }

    }

    @Nested
    @DisplayName("Edge cases and validation tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large numbers")
        void shouldHandleVeryLargeNumbers() {
            BigDecimal largeAmount = new BigDecimal("999999999.99");

            Money money = Money.rub(largeAmount);
            Money result = money.multiply(new BigDecimal("2"));

            assertThat(result.getAmount()).isEqualByComparingTo("1999999999.98");
        }

        @Test
        @DisplayName("Should handle negative amounts")
        void shouldHandleNegativeAmounts() {
            Money money = Money.rub(-100.00);

            Money result = money.multiply(new BigDecimal("2"));

            assertThat(result.getAmount()).isEqualByComparingTo("-200.00");
        }

    }

    @Nested
    @DisplayName("Null handling tests")
    class NullHandlingTests {

        @Test
        @DisplayName("Should throw NullPointerException when amount is null in constructor")
        void shouldThrowExceptionWhenAmountIsNull() {
            assertThatThrownBy(() -> Money.of(null, RUB))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when multiplier is null")
        void shouldThrowExceptionWhenMultiplierIsNull() {
            Money money = Money.rub(100.00);

            assertThatThrownBy(() -> money.multiply(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Additional currency tests")
    class CurrencyTests {

        @Test
        @DisplayName("Should work with different currencies")
        void shouldWorkWithDifferentCurrencies() {
            Currency eur = Currency.getInstance("EUR");
            Currency usd = Currency.getInstance("USD");
            Currency gbp = Currency.getInstance("GBP");

            Money moneyEur = Money.of(100.00, eur);
            Money moneyUsd = Money.of(200.00, usd);
            Money moneyGbp = Money.rub(300.00); // RUB

            assertThat(moneyEur.getCurrency()).isEqualTo(eur);
            assertThat(moneyUsd.getCurrency()).isEqualTo(usd);
            assertThat(moneyGbp.getCurrency()).isEqualTo(RUB);
        }

        @Test
        @DisplayName("Should preserve currency after operations")
        void shouldPreserveCurrencyAfterOperations() {
            Money original = Money.of(100.00, USD);

            Money multiplied = original.multiply(new BigDecimal("2"));

            assertThat(multiplied.getCurrency()).isEqualTo(USD);
        }
    }

}