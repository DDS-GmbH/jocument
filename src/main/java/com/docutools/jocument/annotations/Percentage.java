package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Percentage {

  /**
   * Returns the positions after the comma the value should be formatted with.
   *
   * @return The maximum number of digits after the comma the value should be printed with.
   */
  int maxFractionDigits() default -1;
}
