package com.calpay.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable value object representing a monetary amount with currency.
 * 
 * <p>This class uses {@link BigDecimal} for precise financial calculations,
 * avoiding floating-point precision errors that could lead to accounting
 * discrepancies. All monetary operations return new instances, ensuring
 * immutability and thread safety.
 * 
 * <p><b>Why immutable?</b> Money values should never change once created.
 * This prevents accidental modification bugs and makes the code easier to
 * reason about. Instead of mutating, we return new Money instances.
 * 
 * <p><b>Why BigDecimal?</b> Floating-point types (double, float) cannot
 * accurately represent all decimal values, leading to rounding errors
 * in financial calculations. BigDecimal provides exact precision.
 * 
 * <p><b>Example Usage:</b>
 * <pre>{@code
 * Money balance = Money.of(1000.00, "ZAR");
 * Money transfer = Money.of(250.50, "ZAR");
 * Money newBalance = balance.subtract(transfer);
 * 
 * if (balance.isGreaterThan(transfer)) {
 *     // Sufficient funds
 * }
 * }</pre>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2026/01/06
 */
public final class Money {
    
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    
    private final BigDecimal amount;
    private final String currency;
    
    /**
     * Private constructor to enforce factory method usage.
     * 
     * <p><b>Why private?</b> Ensures all Money instances are validated
     * through factory methods, preventing invalid Money objects from
     * being created directly.
     * 
     * @param amount the monetary amount
     * @param currency the ISO 4217 currency code
     */
    private Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null")
                             .setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
    }
    
    /**
     * Creates a Money instance from a BigDecimal amount.
     * 
     * <p><b>Why static factory?</b> Provides validation, clear naming,
     * and flexibility to return cached instances for common values
     * (like ZERO) to reduce object creation.
     * 
     * @param amount the monetary amount
     * @param currency the ISO 4217 currency code (e.g., "USD", "ZAR")
     * @return a new Money instance
     * @throws NullPointerException if amount or currency is null
     * @throws IllegalArgumentException if currency code is invalid
     */
    public static Money of(BigDecimal amount, String currency) {
        validateCurrencyCode(currency);
        return new Money(amount, currency.toUpperCase());
    }
    
    /**
     * Creates a Money instance from a double value.
     * 
     * <p><b>Warning:</b> Use this method with caution. While convenient,
     * doubles have precision limitations. For user input, prefer parsing
     * from strings. This method is primarily for testing and demos.
     * 
     * <p><b>Example:</b>
     * <pre>{@code
     * Money amount = Money.of(100.50, "ZAR");
     * }</pre>
     * 
     * @param amount the monetary amount as a double
     * @param currency the ISO 4217 currency code
     * @return a new Money instance
     */
    public static Money of(double amount, String currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }
    
    /**
     * Creates a Money instance from a long value (in minor units).
     * 
     * <p><b>Why minor units?</b> Useful when working with APIs that
     * represent money in cents. For example, 10050 cents = 100.50 ZAR.
     * 
     * <p><b>Example:</b>
     * <pre>{@code
     * Money amount = Money.ofMinor(10050, "ZAR");  // 100.50 ZAR
     * }</pre>
     * 
     * @param amountInMinorUnits the amount in minor units (cents)
     * @param currency the ISO 4217 currency code
     * @return a new Money instance
     */
    public static Money ofMinor(long amountInMinorUnits, String currency) {
        BigDecimal amount = BigDecimal.valueOf(amountInMinorUnits)
                                      .divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING);
        return of(amount, currency);
    }
    
    /**
     * Returns a Money instance representing zero in the given currency.
     * 
     * <p><b>Why this method?</b> Common use case for initializing accounts,
     * calculating totals, etc. Provides a clear, readable way to express
     * "no money".
     * 
     * @param currency the currency code
     * @return zero money in the specified currency
     */
    public static Money zero(String currency) {
        return of(BigDecimal.ZERO, currency);
    }
    
    /**
     * Adds another Money amount to this one.
     * 
     * <p><b>Currency matching:</b> Both amounts must be in the same currency.
     * Adding USD to ZAR without conversion would be a business logic error.
     * 
     * <p><b>Why return new instance?</b> Maintains immutability. The original
     * Money objects are unchanged, preventing side effects.
     * 
     * @param other the amount to add
     * @return a new Money instance with the sum
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtracts another Money amount from this one.
     * 
     * <p><b>Negative results allowed:</b> This method doesn't prevent
     * negative balances. That's a business rule enforced by the Account
     * or Service layer, not by the Money value object itself.
     * 
     * @param other the amount to subtract
     * @return a new Money instance with the difference
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    /**
     * Multiplies this Money amount by a scalar factor.
     * 
     * <p><b>Use cases:</b> Calculating interest, applying exchange rates,
     * computing fees as percentages, etc.
     * 
     * <p><b>Why BigDecimal factor?</b> Maintains precision throughout
     * the calculation chain. Using double would introduce rounding errors.
     * 
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     */
    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "Factor cannot be null");
        return new Money(this.amount.multiply(factor), this.currency);
    }
    
    /**
     * Multiplies this Money amount by a double factor.
     * 
     * <p><b>Convenience method</b> for common cases. Prefer the BigDecimal
     * version for maximum precision.
     * 
     * @param factor the multiplication factor
     * @return a new Money instance with the product
     */
    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }
    
    /**
     * Divides this Money amount by a scalar divisor.
     * 
     * <p><b>Rounding:</b> Uses HALF_EVEN rounding (banker's rounding) to
     * minimize cumulative rounding errors over many operations.
     * 
     * @param divisor the division factor
     * @return a new Money instance with the quotient
     * @throws ArithmeticException if divisor is zero
     */
    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor cannot be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new Money(this.amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING), this.currency);
    }
    
    /**
     * Divides this Money amount by a double divisor.
     * 
     * @param divisor the division factor
     * @return a new Money instance with the quotient
     * @throws ArithmeticException if divisor is zero
     */
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }
    
    /**
     * Returns the absolute value of this Money amount.
     * 
     * <p><b>Use case:</b> Converting negative values to positive, e.g.,
     * for display purposes or calculating total debits.
     * 
     * @return a new Money instance with absolute value
     */
    public Money abs() {
        return new Money(this.amount.abs(), this.currency);
    }
    
    /**
     * Returns the negated value of this Money amount.
     * 
     * <p><b>Use case:</b> Converting credits to debits or vice versa
     * in double-entry bookkeeping.
     * 
     * @return a new Money instance with negated value
     */
    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }
    
    /**
     * Checks if this Money amount is greater than another.
     * 
     * <p><b>Why comparison methods?</b> Provides type-safe, readable
     * comparisons. More intuitive than using compareTo() == 1.
     * 
     * @param other the amount to compare against
     * @return true if this amount is greater than other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Checks if this Money amount is greater than or equal to another.
     * 
     * @param other the amount to compare against
     * @return true if this amount is >= other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isGreaterThanOrEqualTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    /**
     * Checks if this Money amount is less than another.
     * 
     * @param other the amount to compare against
     * @return true if this amount is less than other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    /**
     * Checks if this Money amount is less than or equal to another.
     * 
     * @param other the amount to compare against
     * @return true if this amount is <= other
     * @throws IllegalArgumentException if currencies don't match
     */
    public boolean isLessThanOrEqualTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }
    
    /**
     * Checks if this Money amount is zero.
     * 
     * @return true if amount is zero
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Checks if this Money amount is positive.
     * 
     * @return true if amount is greater than zero
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Checks if this Money amount is negative.
     * 
     * @return true if amount is less than zero
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * Checks if this Money amount is zero or negative.
     * 
     * <p><b>Why this method?</b> Common validation check for transfers
     * and payments. Provides clear, readable intent compared to
     * comparing with zero explicitly.
     * 
     * @return true if amount is less than or equal to zero
     */
    public boolean isNegativeOrZero() {
        return this.amount.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    /**
     * Returns the raw BigDecimal amount.
     * 
     * <p><b>Use sparingly:</b> Exposes internal representation. Prefer
     * using Money's own methods (add, subtract, etc.) to maintain
     * type safety and prevent currency mismatches.
     * 
     * @return the monetary amount as BigDecimal
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Returns the amount as a double.
     * 
     * <p><b>Warning:</b> May lose precision. Use only for display or
     * interfacing with legacy systems that require double.
     * 
     * @return the amount as a double
     */
    public double getAmountAsDouble() {
        return amount.doubleValue();
    }
    
    /**
     * Returns the amount in minor units (cents).
     * 
     * <p><b>Use case:</b> For APIs that expect money in cents, e.g.,
     * payment gateways. 100.50 ZAR → 10050 cents.
     * 
     * @return the amount in minor units
     */
    public long getAmountInMinorUnits() {
        return amount.multiply(BigDecimal.valueOf(100))
                     .setScale(0, DEFAULT_ROUNDING)
                     .longValue();
    }
    
    /**
     * Returns the ISO 4217 currency code.
     * 
     * @return the currency code (e.g., "USD", "ZAR")
     */
    public String getCurrency() {
        return currency;
    }
    
    /**
     * Validates that another Money object has the same currency.
     * 
     * <p><b>Why fail fast?</b> Catching currency mismatches early
     * prevents confusing errors downstream. Clear exception messages
     * help developers fix bugs quickly.
     * 
     * @param other the Money to compare currency with
     * @throws IllegalArgumentException if currencies differ
     */
    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Other money cannot be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: %s != %s", 
                    this.currency, other.currency)
            );
        }
    }
    
    /**
     * Validates ISO 4217 currency code format.
     * 
     * <p><b>Why validate?</b> Prevents invalid currency codes from
     * propagating through the system. While we could maintain a full
     * list of valid codes, that list changes over time. This validates
     * the format only (3 uppercase letters).
     * 
     * @param currency the currency code to validate
     * @throws IllegalArgumentException if format is invalid
     */
    private static void validateCurrencyCode(String currency) {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                "Invalid currency code. Must be 3 uppercase letters: " + currency
            );
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        // Use compareTo for BigDecimal to handle different scales (2.00 == 2.0)
        return amount.compareTo(money.amount) == 0 && 
               currency.equals(money.currency);
    }
    
    @Override
    public int hashCode() {
        // Strip trailing zeros for consistent hashing (2.00 and 2.0 hash the same)
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
    
    @Override
    public String toString() {
        return String.format("%s %.2f", currency, amount);
    }
    
    /**
     * Returns a formatted string suitable for display.
     * 
     * <p><b>Example:</b> "ZAR 1,234.56"
     * 
     * @return formatted money string
     */
    public String toFormattedString() {
        return String.format("%s %,.2f", currency, amount);
    }
}