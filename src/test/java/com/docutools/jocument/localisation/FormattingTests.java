package com.docutools.jocument.localisation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("Formatting")
@Tag("automated")
class FormattingTests {

  @Test
  @DisplayName("Register custom formatting for temporal types.")
  void registerCustomFormatterForTemporalTypes() {
    // Arrange
    var dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    var resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);
    var options = new GenerationOptionsBuilder()
        .format(LocalDate.class, dtf::format)
        .build();
    resolver.setOptions(options);

    // Act
    var actual = resolver.resolve("built")
        .orElseThrow()
        .toString();

    // Assert
    var expected = dtf.format(SampleModelData.ENTERPRISE.built());
    assertThat(actual, equalTo(expected));
  }

  @Test
  void formatObjects() {
    // Arrange
    var dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    var options = new GenerationOptionsBuilder()
        .format(LocalDate.class, dtf::format)
        .build();

    // Act
    Optional<String> formattedDate = options.tryToFormat(Locale.ENGLISH, LocalDate.now());

    // Assert
    assertThat(formattedDate.isPresent(), is(true));
    assertThat(formattedDate.get(), matchesPattern(Pattern.compile("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d")));
  }
}
