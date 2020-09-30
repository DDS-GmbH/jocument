package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Money {

  /**
   * Returns the currency of the money value.
   *
   * @return the currency code of the currencythe money value should be expressed with
   */
  String currencyCode() default "";
}
