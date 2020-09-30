package com.docutools.jocument.localisation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import com.docutools.jocument.impl.word.WordUtilities;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Detect Document Languages")
public class DetectDocumentLanguage {

  @Test
  @DisplayName("Find german and english from GermanEnglishTemplate.docx")
  void shouldDetectGermanAndEnglishFromDocx() throws IOException {
    // Arrange
    try (var in = new BufferedInputStream(
        getClass().getResourceAsStream("/templates/word/GermanEnglishTemplate.docx"))) {
      var document = new XWPFDocument(in);

      // Act
      Collection<Locale> actualLanguages = WordUtilities.detectLanguages(document);

      // Assert
      assertThat(actualLanguages, hasItems(Locale.forLanguageTag("en-GB"),
          Locale.forLanguageTag("de-AT")));
    }
  }

  @Test
  @DisplayName("Find german and english from paragraphs in tables from GermanEnglishTableTemplate.docx")
  void shouldDetectGermanAndEnglishFromTableInDocx() throws IOException {
    // Arrange
    try (var in = new BufferedInputStream(
        getClass().getResourceAsStream("/templates/word/GermanEnglishTableTemplate.docx"))) {
      var document = new XWPFDocument(in);

      // Act
      Collection<Locale> actualLanguages = WordUtilities.detectLanguages(document);

      // Assert
      assertThat(actualLanguages, hasItems(Locale.forLanguageTag("en-GB"),
          Locale.forLanguageTag("de-AT")));
    }
  }

  @Test
  @DisplayName("Detect English correctly as most common locale")
  void shouldDetectEnglishAsMostCommonLocale() throws IOException {
    // Arrange
    try (var in = new BufferedInputStream(
        getClass().getResourceAsStream("/templates/word/GermanEnglishTableTemplate.docx"))) {
      var document = new XWPFDocument(in);

      // Act
      Locale mostCommonLocale = WordUtilities.detectMostCommonLocale(document).get();

      // Assert
      assertThat(mostCommonLocale, Matchers.equalTo(Locale.forLanguageTag("en-GB")));
    }
  }

}
