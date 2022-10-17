package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Translatable {
  /**
   * Specify the method which should be applied to retrieve a (translatable) string from the annotated object.
   *
   * @return The name of the method to use to retrieve the string which should be translated from objects
   */
  String toStringMethod() default "toString";
}
