package com.docutools.jocument.localisation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Formatting")
class Formatting {

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

}
