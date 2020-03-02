package com.docutools.jocument.impl;

import java.lang.annotation.Annotation;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.Set;

public class ReflectionUtils {

  private static final Set<Class<?>> NUMERIC_PRIMITIVES =
          Set.of(byte.class, short.class, int.class, long.class, float.class, double.class);

  public static boolean isJsr310Type(Class<?> type) {
    return Temporal.class.isAssignableFrom(type);
  }

  /**
   * Gets the annotation instance on the given field in the base class.
   *
   * @param baseClass the base class
   * @param fieldName the field name
   * @param annotationType the type of the annotation
   * @param <A> the type of the annotation
   * @return the annotated instance on the field
   */
  public static <A extends Annotation> Optional<A> findFieldAnnotation(Class<?> baseClass, String fieldName, Class<A> annotationType) {
    try {
      return Optional.ofNullable(baseClass.getDeclaredField(fieldName)
              .getDeclaredAnnotation(annotationType));
    } catch (NoSuchFieldException e) {
      return Optional.empty();
    }
  }

  public static boolean isNumeric(Class<?> type) {
    return type != null &&
            (Number.class.isAssignableFrom(type) || NUMERIC_PRIMITIVES.contains(type));
  }

  private ReflectionUtils() {
  }
}
