package com.docutools.jocument.excel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.docutools.jocument.Document;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import java.awt.Desktop;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Generating Excel Documents")
public class ExcelDocuments {

  @Test
  @DisplayName("Clone a simple excel file.")
  void shouldCloneSimpleExcel() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/SimpleDocument.xlsx")
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
  @DisplayName("Clone a simple excel file with a loop.")
  void shouldCloneSimpleExcelWithLoop() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/SimpleDocumentWithLoop.xlsx")
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
  @DisplayName("Resolve collection placeholders.")
  void shouldResolveCollectionPlaceholders() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/CollectionsTemplate.xlsx")
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
  @DisplayName("Resolve user profile placeholders.")
  void shouldResolveUserProfilePlaceholders() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/UserProfileTemplate.xlsx")
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
  @DisplayName("Process formulas.")
  void shouldCopyFormulas() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/FormulaTemplate.xlsx")
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
  @DisplayName("Resolve nested loops.")
  void shouldResolveNestedLoops() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/NestedLoopDocument.xlsx")
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
  @DisplayName("Inherit images from source excel.")
  void inherti() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/Formatting.xlsx")
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
  @DisplayName("Inherit style from source excel.")
  void inheritStylePerCell() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/ComplexFormatting.xlsx")
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
  @DisplayName("Keep Auto Filter")
  void keepAutoFilters() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/AutoFilters.xlsx")
        .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(5_000L); // 5 seconds

    // Assert
    assertThat(document.completed(), is(true));
    Desktop.getDesktop().open(document.getPath().toFile());
  }

  @Test
  @DisplayName("Keep Cell Formatting")
  void keepCellFormatting() throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath("/templates/excel/CellFormatting.xlsx")
        .orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

    // Act
    Document document = template.startGeneration(resolver);
    document.blockUntilCompletion(720_000L); // 5 seconds

    // Assert
    assertThat(document.completed(), is(true));
    Desktop.getDesktop().open(document.getPath().toFile());
  }
}
