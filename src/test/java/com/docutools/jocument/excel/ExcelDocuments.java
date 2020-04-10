package com.docutools.jocument.excel;

import com.docutools.jocument.Document;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DisplayName("Generating Excel Documents")
public class ExcelDocuments {

    @Test
    @DisplayName("Clone a simple excel file.")
    void shouldCloneSimpleExcel() throws InterruptedException, IOException {
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
    @DisplayName("Resolve picture placeholder.")
    void shouldResolvePicturePlaceholder() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/PictureTemplate.xlsx")
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

}
