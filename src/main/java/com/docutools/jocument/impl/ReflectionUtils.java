package com.docutools.jocument.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.Set;

public class ReflectionUtils {
  private static final Logger logger = LogManager.getLogger();

  public static boolean isJsr310Type(Class<?> type) {
    return Temporal.class.isAssignableFrom(type);
  }

  private static final Set<Class<?>> WRAPPER_TYPES = Set.of(Boolean.class,
          Character.class,
          Byte.class,
          Short.class,
          Integer.class,
          Long.class,
          Float.class,
          Double.class,
          Void.class);

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
    logger.debug("Searching for annotation {} in class {}", fieldName, baseClass);
    try {
      return Optional.ofNullable(baseClass.getDeclaredField(fieldName)
              .getDeclaredAnnotation(annotationType));
    } catch (NoSuchFieldException e) {
      logger.info("Did not find annotation {} in class {}", fieldName, baseClass);
      return Optional.empty();
    }
  }

  public static boolean isWrapperType(Class<?> class_) {
    return WRAPPER_TYPES.contains(class_);
  }

  private ReflectionUtils() {
  }
}
