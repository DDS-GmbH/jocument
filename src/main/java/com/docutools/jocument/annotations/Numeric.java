package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.RoundingMode;

@Retention(RetentionPolicy.RUNTIME)
public @interface Numeric {
  /**
   * Sets the maximum number of digits allowed in the fraction portion of a number. maximumFractionDigits must
   * be >= minimumFractionDigits. If the new value for maximumFractionDigits is less than the current value of
   * minimumFractionDigits, then minimumFractionDigits will also be set to the new value.
   *
   * @return the maximum fraction digits
   * @see java.text.NumberFormat#setMaximumFractionDigits(int)
   */
  int maxFractionDigits() default -1;

  /**
   * Sets the minimum number of digits allowed in the fraction portion of a number. minimumFractionDigits must
   * be <= maximumFractionDigits. If the new value for minimumFractionDigits exceeds the current value of
   * maximumFractionDigits, then maximumIntegerDigits will also be set to the new value
   *
   * @return the minimum fraction digits
   * @see java.text.NumberFormat#setMinimumFractionDigits(int)
   */
  int minFractionDigits() default -1;
  int maxIntDigits() default -1;
  int minIntDigits() default -1;
  String currencyCode() default "";
  boolean groupingUsed() default false;
  boolean parseIntegerOnly() default false;
  RoundingMode roundingMode() default RoundingMode.HALF_UP; // TODO what is the default value for RoundingMode?
}
