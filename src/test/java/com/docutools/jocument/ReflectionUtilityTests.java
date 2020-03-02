package com.docutools.jocument;

import com.docutools.jocument.impl.ReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.zip.Adler32;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Reflection Utilities")
public class ReflectionUtilityTests {

  @ParameterizedTest(name = "Detect JSR 310 Types")
  @ValueSource(classes = {ZonedDateTime.class, LocalDateTime.class, LocalDate.class, Instant.class})
  void shouldDetectJSR310Types(Class<?> jsr310Type) {
    // Act
    boolean jsr310 = ReflectionUtils.isJsr310Type(jsr310Type);
    // Assert
    assertThat(jsr310, is(true));
  }

  @ParameterizedTest(name = "Negatively detect non-JSR 310 Types")
  @ValueSource(classes = {Duration.class, Object.class, Adler32.class, String.class})
  void shouldNegativelyDetectNonJsr310Types(Class<?> jsr310Type) {
    // Act
    boolean jsr310 = ReflectionUtils.isJsr310Type(jsr310Type);
    // Assert
    assertThat(jsr310, is(false));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TheAnnotation {}

  static class Clazz {
    @TheAnnotation
    private Object field;
  }

  @Test
  void shouldGetAnnotationFromPrivateField() {
    // Arrange
    var clazz = Clazz.class;
    // Act
    Optional<TheAnnotation> annotation = ReflectionUtils.findFieldAnnotation(clazz, "field", TheAnnotation.class);
    // Assert
    assertThat(annotation, notNullValue());
    assertThat(annotation.isPresent(), is(true));
  }

  @Test
  void shouldGetEmptyResultWhenAnnotationNotOnField() {
    // Act
    var result = ReflectionUtils.findFieldAnnotation(Clazz.class, "field", Override.class);
    // Assert
    assertThat(result, notNullValue());
    assertThat(result.isEmpty(), is(true));
  }

  @ParameterizedTest(name = "Should detect {0} as numeric type.")
  @ValueSource(classes = {
          byte.class, short.class, int.class, long.class, float.class, double.class,
          Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class
  })
  void shouldDetectNumericTypes(Class<?> type) {
    // Act + Assert
    assertThat(ReflectionUtils.isNumeric(type), is(true));
  }

  @ParameterizedTest(name = "Should detect {0} as non-numeric type.")
  @ValueSource(classes = {Object.class, String.class, Adler32.class, Date.class})
  void shouldDetectNonNumericTypes(Class<?> type) {
    // Act + Assert
    assertThat(ReflectionUtils.isNumeric(type), is(false));
  }
}
