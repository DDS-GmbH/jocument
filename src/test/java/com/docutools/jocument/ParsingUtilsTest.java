package com.docutools.jocument;

import com.docutools.jocument.impl.ParsingUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Parsing Utils")
public class ParsingUtilsTest {

  @Test
  @DisplayName("Strip brackets from placeholder.")
  void shouldStripBrackets() {
    // Arrange
    var placeholder = "{{placeholder}}";
    // Act
    var placeholderName = ParsingUtils.stripBrackets(placeholder);
    // Assert
    assertThat(placeholderName, equalTo("placeholder"));
  }

}
