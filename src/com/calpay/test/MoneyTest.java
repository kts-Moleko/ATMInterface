package com.calpay.test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.calpay.core.model.Money;
import com.calpay.core.exceptions.InvalidRequestException;

import java.math.BigDecimal;

/**
 * Unit tests for Money value object.
 * 
 * <p><b>Test Categories:</b>
 * <ul>
 *   <li>Creation and validation</li>
 *   <li>Arithmetic operations</li>
 *   <li>Comparison operations</li>
 *   <li>Currency validation</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {
    
    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {
        
        @Test
        @DisplayName("Should create Money from BigDecimal")
        void shouldCreateFromBigDecimal() {
            Money money = Money.of(new BigDecimal("100.50"), "ZAR");
            
            assertEquals(new BigDecimal("100.50"), money.getAmount());
            assertEquals("ZAR", money.getCurrency());
        }
        
        @Test
        @DisplayName("Should create Money from double")
        void shouldCreateFromDouble() {
            Money money = Money.of(100.50, "ZAR");
            
            assertEquals(100.50, money.getAmountAsDouble(), 0.01);
            assertEquals("ZAR", money.getCurrency());
        }
        
        @Test
        @DisplayName("Should create Money from minor units (cents)")
        void shouldCreateFromMinorUnits() {
            Money money = Money.ofMinor(10050, "ZAR");
            
            assertEquals(new BigDecimal("100.50"), money.getAmount());
            assertEquals(10050, money.getAmountInMinorUnits());
        }
        
        @Test
        @DisplayName("Should create zero Money")
        void shouldCreateZero() {
            Money zero = Money.zero("ZAR");
            
            assertTrue(zero.isZero());
            assertEquals(new BigDecimal("0.00"), zero.getAmount());
        }
        
        @Test
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount() {
            assertThrows(NullPointerException.class, () -> {
                Money.of(null, "ZAR");
            });
        }
        
        @Test
        @DisplayName("Should reject null currency")
        void shouldRejectNullCurrency() {
            assertThrows(NullPointerException.class, () -> {
                Money.of(100.00, null);
            });
        }
        
        @Test
        @DisplayName("Should reject invalid currency code")
        void shouldRejectInvalidCurrency() {
            assertThrows(IllegalArgumentException.class, () -> {
                Money.of(100.00, "INVALID");
            });
        }
    }
    
    @Nested
    @DisplayName("Arithmetic Tests")
    class ArithmeticTests {
        
        private Money hundred;
        private Money fifty;
        
        @BeforeEach
        void setUp() {
            hundred = Money.of(100.00, "ZAR");
            fifty = Money.of(50.00, "ZAR");
        }
        
        @Test
        @DisplayName("Should add Money amounts")
        void shouldAdd() {
            Money result = hundred.add(fifty);
            
            assertEquals(new BigDecimal("150.00"), result.getAmount());
            assertEquals("ZAR", result.getCurrency());
        }
        
        @Test
        @DisplayName("Should subtract Money amounts")
        void shouldSubtract() {
            Money result = hundred.subtract(fifty);
            
            assertEquals(new BigDecimal("50.00"), result.getAmount());
        }
        
        @Test
        @DisplayName("Should multiply by factor")
        void shouldMultiply() {
            Money result = hundred.multiply(2.5);
            
            assertEquals(new BigDecimal("250.00"), result.getAmount());
        }
        
        @Test
        @DisplayName("Should divide by factor")
        void shouldDivide() {
            Money result = hundred.divide(2.0);
            
            assertEquals(new BigDecimal("50.00"), result.getAmount());
        }
        
        @Test
        @DisplayName("Should calculate absolute value")
        void shouldCalculateAbsoluteValue() {
            Money negative = Money.of(-50.00, "ZAR");
            Money result = negative.abs();
            
            assertEquals(new BigDecimal("50.00"), result.getAmount());
        }
        
        @Test
        @DisplayName("Should negate value")
        void shouldNegate() {
            Money result = hundred.negate();
            
            assertEquals(new BigDecimal("-100.00"), result.getAmount());
        }
        
        @Test
        @DisplayName("Should reject arithmetic with different currencies")
        void shouldRejectDifferentCurrencies() {
            Money usd = Money.of(100.00, "USD");
            
            assertThrows(IllegalArgumentException.class, () -> {
                hundred.add(usd);
            });
        }
        
        @Test
        @DisplayName("Should reject division by zero")
        void shouldRejectDivisionByZero() {
            assertThrows(ArithmeticException.class, () -> {
                hundred.divide(0.0);
            });
        }
    }
    
    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {
        
        private Money hundred;
        private Money fifty;
        private Money anotherHundred;
        
        @BeforeEach
        void setUp() {
            hundred = Money.of(100.00, "ZAR");
            fifty = Money.of(50.00, "ZAR");
            anotherHundred = Money.of(100.00, "ZAR");
        }
        
        @Test
        @DisplayName("Should compare greater than")
        void shouldCompareGreaterThan() {
            assertTrue(hundred.isGreaterThan(fifty));
            assertFalse(fifty.isGreaterThan(hundred));
        }
        
        @Test
        @DisplayName("Should compare less than")
        void shouldCompareLessThan() {
            assertTrue(fifty.isLessThan(hundred));
            assertFalse(hundred.isLessThan(fifty));
        }
        
        @Test
        @DisplayName("Should compare greater than or equal")
        void shouldCompareGreaterThanOrEqual() {
            assertTrue(hundred.isGreaterThanOrEqualTo(fifty));
            assertTrue(hundred.isGreaterThanOrEqualTo(anotherHundred));
        }
        
        @Test
        @DisplayName("Should identify zero")
        void shouldIdentifyZero() {
            Money zero = Money.zero("ZAR");
            assertTrue(zero.isZero());
            assertFalse(hundred.isZero());
        }
        
        @Test
        @DisplayName("Should identify positive")
        void shouldIdentifyPositive() {
            assertTrue(hundred.isPositive());
            assertFalse(Money.zero("ZAR").isPositive());
        }
        
        @Test
        @DisplayName("Should identify negative")
        void shouldIdentifyNegative() {
            Money negative = Money.of(-50.00, "ZAR");
            assertTrue(negative.isNegative());
            assertFalse(hundred.isNegative());
        }
        
        @Test
        @DisplayName("Should check equality")
        void shouldCheckEquality() {
            assertEquals(hundred, anotherHundred);
            assertNotEquals(hundred, fifty);
        }
    }
    
    @Nested
    @DisplayName("Formatting Tests")
    class FormattingTests {
        
        @Test
        @DisplayName("Should format as string")
        void shouldFormatAsString() {
            Money money = Money.of(1234.56, "ZAR");
            
            assertEquals("ZAR 1234.56", money.toString());
        }
        
        @Test
        @DisplayName("Should format with thousands separator")
        void shouldFormatWithThousandsSeparator() {
            Money money = Money.of(1234.56, "ZAR");
            
            assertEquals("ZAR 1,234.56", money.toFormattedString());
        }
    }
}