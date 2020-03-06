package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.RoundingMode;
import java.util.Currency;

@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric {
  /**
   * Sets the maximum number of digits allowed in the fraction portion of a number. maxFractionDigits must
   * be >= minFractionDigits. If the new value for maxFractionDigits is less than the current value of
   * minFractionDigits, then minFractionDigits will also be set to the new value.
   *
   * @return the maximum fraction digits
   * @see java.text.NumberFormat#setMaximumFractionDigits(int)
   */
  int maxFractionDigits() default -1;

  /**
   * Sets the minimum number of digits allowed in the fraction portion of a number. minFractionDigits must
   * be <= maxFractionDigits. If the new value for minFractionDigits exceeds the current value of
   * maxFractionDigits, then maxFractionDigits will also be set to the new value
   *
   * @return the minimum fraction digits
   * @see java.text.NumberFormat#setMinimumFractionDigits(int)
   */
  int minFractionDigits() default -1;

  /**
   * Sets the maximum number of digits allowed in the integer portion of a number. maxIntDigits must
   * be >= minIntDigits. If the new value for maxIntDigits is less than the current value of
   * minIntDigits, then minIntDigits will also be set to the new value.
   *
   * @return the maximum integer digits
   * @see java.text.NumberFormat#setMaximumIntegerDigits(int)
   */
  int maxIntDigits() default -1;

  /**
   * Sets the minimum number of digits allowed in the integer portion of a number. minIntDigits must
   * be <= maxIntDigits. If the new value for minIntDigits exceeds the current value of
   * maxIntDigits, then maxIntDigits will also be set to the new value
   *
   * @return the minimum integer digits
   * @see java.text.NumberFormat#setMinimumIntegerDigits(int)
   */
  int minIntDigits() default -1;

  /**
   * Sets the currency code of the currency to use when formatting this Numeric
   * @return The currency code
   * @see java.text.NumberFormat#setCurrency(Currency)
   * @see java.util.Currency#getInstance(String)
   */
  String currencyCode() default "";

  /**
   * Sets whether grouping should be used.
   * For example, in the English locale, with grouping on, the number 1234567 might be formatted as "1,234,567".
   * The grouping separator as well as the size of each group is locale dependent and is determined by
   * sub-classes of NumberFormat.
   * @return true if grouping is used; false otherwise
   * @see java.text.NumberFormat
   */
  boolean groupingUsed() default false;

  /**
   * Sets whether only the integer part should be parsed.
   * E.g: if true, "345.67" -> 345
   * @return true if only the integer part is parsed, false otherwise
   * @see java.text.NumberFormat#isParseIntegerOnly()
   */
  boolean parseIntegerOnly() default false;

  /**
   * Sets the rounding mode to use when rounding is necessary.
   * @return The used rounding mode
   * @see java.text.NumberFormat#setRoundingMode(RoundingMode)
   * @see java.math.RoundingMode
   */
  RoundingMode roundingMode() default RoundingMode.UNNECESSARY;
}
