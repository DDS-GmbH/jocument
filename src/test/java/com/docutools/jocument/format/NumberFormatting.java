package com.docutools.jocument.format;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.annotations.Money;
import com.docutools.jocument.annotations.Numeric;
import com.docutools.jocument.annotations.Percentage;
import com.docutools.jocument.impl.ReflectionResolver;
import java.math.RoundingMode;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Format Numbers by Locale")
class NumberFormatting {

  @Test
  @DisplayName("Format floating point numbers using commas.")
  void shouldFormatByUSStandards() {
    // Arrange
    var instance = new Clazz();
    instance.d = 12345678.9;
    var resolver = new ReflectionResolver(instance);
    // Act
    var actual = resolver.resolve("d", Locale.US)
        .map(PlaceholderData::toString)
        .orElse("");
    // Assert
    assertThat(actual, equalTo("12,345,678.9"));
  }

  @Test
  @DisplayName("Format float point fields annotated with @Percentage as percentage.")
  void shouldFormatPercentage() {
    // Arrange
    var instance = new Clazz();
    instance.p = 0.555;
    var resolver = new ReflectionResolver(instance);
    // Act
    var actual = resolver.resolve("p")
        .map(PlaceholderData::toString)
        .orElse("");
    // Assert
    assertThat(actual, equalTo("55,5\u00A0%"));
  }

  @Test
  @DisplayName("Format floating point fields annoted with @Money as monetary amount.")
  void shouldFormatMoney() {
    // Arrange
    var instance = new Clazz();
    instance.c = 13.75;
    var resolver = new ReflectionResolver(instance);
    // Act
    var actual = resolver.resolve("c", Locale.GERMAN)
        .map(PlaceholderData::toString)
        .orElse("");
    // Assert
    assertThat(actual, equalTo("13,75\u00A0€"));
  }

  @Test
  @DisplayName("Format numeric with the correct amount of integer places")
  void shouldFormatNumericMaxIntDigits() {
    // Arrange
    var instance = new Clazz();
    instance.f = 1123456.45675;
    var resolver = new ReflectionResolver(instance);

    // Act
    var actual = resolver.resolve("f", Locale.GERMAN)
        .map(PlaceholderData::toString)
        .orElse("");

    // Assert
    assertThat(actual, equalTo("123.456,4567"));
  }

  public static class Clazz {
    private double d;
    @Percentage(maxFractionDigits = 2)
    private double p;
    @Money(currencyCode = "EUR")
    private double c;
    @Numeric(maxFractionDigits = 4,
        minFractionDigits = 2,
        maxIntDigits = 6,
        minIntDigits = 2,
        currencyCode = "EUR",
        groupingUsed = true,
        parseIntegerOnly = false,
        roundingMode = RoundingMode.HALF_EVEN)
    private double f;

    public double getD() {
      return d;
    }

    public double getP() {
      return p;
    }

    public double getC() {
      return c;
    }

    public double getF() {
      return f;
    }
  }
}
