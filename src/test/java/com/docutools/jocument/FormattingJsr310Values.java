package com.docutools.jocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.docutools.jocument.annotations.Format;
import com.docutools.jocument.impl.ReflectionResolver;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Formatting JSR-310 Values")
class FormattingJsr310Values {

  @Test
  @DisplayName("Format LocalDate")
  void shouldFormatLocalDate() {
    // Arrange
    var object = new Jsr310Wrappers();
    object.date = LocalDate.of(2020, 1, 1);
    var resolver = new ReflectionResolver(object);

    // Act
    String actual = resolver.resolve("date")
        .map(PlaceholderData::toString)
        .orElse("");

    // Assert
    assertThat(actual, equalTo("1. Jan, 2020"));
  }

  @Test
  @DisplayName("Format LocalDateTime")
  void shouldFormatLocalDateTime() {
    // Arrange
    var object = new Jsr310Wrappers();
    object.dateTime = LocalDateTime.of(2020, 1, 1, 12, 30, 45);
    var resolver = new ReflectionResolver(object);
    // Act
    String actual = resolver.resolve("dateTime")
        .map(PlaceholderData::toString)
        .orElse("");
    // Assert
    assertThat(actual, equalTo("20/1/1 12:30:45"));
  }

  @Test
  @DisplayName("Format Instant")
  void shouldFormatInstant() {
    // Arrange
    var object = new Jsr310Wrappers();
    object.instant = LocalDateTime.of(2020, 1, 1, 12, 30, 45).toInstant(ZoneOffset.UTC);
    var resolver = new ReflectionResolver(object);
    // Act
    String actual = resolver.resolve("instant")
        .map(PlaceholderData::toString)
        .orElse("");
    // Assert
    assertThat(actual, equalTo("12:30:45.000"));
  }

  @Test
  @DisplayName("Format ZonedDateTime with Locale")
  void shouldFormatZonedDateTimeWithLocale() {
    // Arrange
    var object = new Jsr310Wrappers();
    object.zonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 30, 45, 0, ZoneId.of("Europe/Vienna"));
    var resolver = new ReflectionResolver(object);
    // Act
    String actual = resolver.resolve("zonedDateTime")
        .map(PlaceholderData::toString)
        .orElse("");
    // Assert
    assertThat(actual, equalTo("Jänner 20"));
  }

  public static class Jsr310Wrappers {
    @Format("d. MMM, yyyy")
    private LocalDate date;
    @Format("yy/M/d h:m:s")
    private LocalDateTime dateTime;
    @Format("H:m:s.SSS")
    private Instant instant;
    @Format(value = "MMMM yy", locale = "de-AT")
    private ZonedDateTime zonedDateTime;

    public LocalDate getDate() {
      return date;
    }

    public LocalDateTime getDateTime() {
      return dateTime;
    }

    public Instant getInstant() {
      return instant;
    }

    public ZonedDateTime getZonedDateTime() {
      return zonedDateTime;
    }
  }

}
