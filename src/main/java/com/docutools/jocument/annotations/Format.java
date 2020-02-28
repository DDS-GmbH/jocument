package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Format {
  String value();
  String zone() default "UTC";
  String locale() default "en/US";
}
