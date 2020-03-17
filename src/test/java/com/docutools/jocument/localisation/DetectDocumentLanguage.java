package com.docutools.jocument.localisation;

import com.docutools.jocument.impl.word.WordUtilities;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

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
  @DisplayName("Detect english correctly as most common locale")
  void shouldDetectEnglistAsMostCommonLocale() throws IOException {
    // Arrange
    try (var in = new BufferedInputStream(
            getClass().getResourceAsStream("/templates/word/GermanEnglishTableTemplate.docx"))) {
      var document = new XWPFDocument(in);

      // Act
      Locale mostCommonLocale = WordUtilities.detectMostCommonLocale(document);

      // Assert
      assertThat(mostCommonLocale, Matchers.equalTo(Locale.forLanguageTag("en-GB")));
    }
  }

}
