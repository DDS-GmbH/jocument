package com.docutools.jocument;

import com.docutools.jocument.impl.JsonResolver;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@DisplayName("Generating Word Documents")
public class WordDocuments {


  @Test
  @DisplayName("Generate a document from a simple template.")
  void shouldGenerateSimpleDocument() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/word/UserProfileTemplate.docx")
            .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(60000L); // 1 minute

    // Assert
    assertThat(document.completed(), is(true));

    Desktop.getDesktop().open(document.getPath().toFile());
  }

  @Test
  @DisplayName("Generate a document from a template with multiple locales.")
  void shouldGenerateMultiLocaleDocument() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/word/UserProfileTemplateWithDifferentLocales.docx")
            .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(60000L); // 1 minute

    // Assert
    assertThat(document.completed(), is(true));

    Desktop.getDesktop().open(document.getPath().toFile());
  }


  @Test
  @DisplayName("Replace placeholders in tables.")
  void shouldReplacePlaceholdersInTables() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/word/UserProfileWithTableTemplate.docx")
            .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(60000L); // 1 minute

    // Assert
    assertThat(document.completed(), is(true));

    Desktop.getDesktop().open(document.getPath().toFile());
  }

  @Test
  @DisplayName("Resolve collection placeholders.")
  void shouldResolveCollectionPlaceholders() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/word/CollectionsTemplate.docx")
            .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(60000L); // 1 minute

    // Assert
    assertThat(document.completed(), is(true));

    Desktop.getDesktop().open(document.getPath().toFile());
  }

  @Test
  @DisplayName("Apply custom word placeholder.")
  void shouldApplyCustomWordPlaceholder() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/word/CustomPlaceholderTemplate.docx")
            .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(60000L); // 1 minute

    // Assert
    assertThat(document.completed(), is(true));

    Desktop.getDesktop().open(document.getPath().toFile());
  }

  @Test
  @DisplayName("Apply custom word placeholder from json data.")
  void shouldApplyCustomWordPlaceholderFromJson() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/word/CustomPlaceholderTemplate.docx")
            .orElseThrow();

    String json = TestUtils.getText("json/picard.json");
    PlaceholderResolver resolver = new JsonResolver(json);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(60000L); // 1 minute

    // Assert
    assertThat(document.completed(), is(true));

    Desktop.getDesktop().open(document.getPath().toFile());
  }

  @Test
  @DisplayName("Generate a document from a simple template.")
  void shouldGenerateSimpleDocumentWithTOC() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/word/TOCTemplate.docx")
            .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(60000L); // 1 minute

    // Assert
    assertThat(document.completed(), is(true));

    Desktop.getDesktop().open(document.getPath().toFile());
  }
}
