package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Percentage {
  int maxFractionDigits() default -1;
}
