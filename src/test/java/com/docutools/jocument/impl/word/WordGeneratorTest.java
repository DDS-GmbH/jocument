package com.docutools.jocument.impl.word;

import com.docutools.jocument.Document;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.TestUtils;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.poipath.xwpf.XWPFDocumentWrapper;
import java.awt.Desktop;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@DisplayName("Word Generator Tests")
@Tag("automated")
@Tag("xwpf")
class WordGeneratorTest {
    XWPFDocument xwpfDocument;

    @AfterEach
    void cleanup() throws IOException {
        if (xwpfDocument != null) {
            xwpfDocument.close();
        }
    }

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
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = XWPFDocumentWrapper.parse(xwpfDocument);
        assertThat(documentWrapper.paragraph(0).text(), equalTo("User Profile: Jean-Luc Picard"));
        assertThat(documentWrapper.paragraph(2).text(), equalTo("Name: Jean-Luc"));
        assertThat(documentWrapper.paragraph(3).text(), equalTo("Last Name: Picard"));
        assertThat(documentWrapper.paragraph(4).text(), equalTo("Age: "
                + Period.between(LocalDate.of(1948, 9, 23), LocalDate.now()).getYears()
                + " ("
                + DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23))
                + ")"));
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
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = XWPFDocumentWrapper.parse(xwpfDocument);
        assertThat(documentWrapper.paragraph(0).text(), equalTo("User Profile: Jean-Luc Picard"));
        assertThat(documentWrapper.paragraph(2).text(), equalTo("Name: Jean-Luc"));
        assertThat(documentWrapper.paragraph(3).text(), equalTo("Last Name: Picard"));
        assertThat(documentWrapper.paragraph(4).text(), equalTo("Age German: "
                + Period.between(LocalDate.of(1948, 9, 23), LocalDate.now()).getYears()
                + " ("
                + DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23))
                + ")"));
        assertThat(documentWrapper.paragraph(5).text(), equalTo("Age English: "
                + Period.between(LocalDate.of(1948, 9, 23), LocalDate.now()).getYears()
                + " ("
                + DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23))
                + ")"));
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
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = XWPFDocumentWrapper.parse(xwpfDocument);
        var table = documentWrapper.table(0);
        var birthdate = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23));
        assertThat(table.row(0).column(0).paragraph(0).text(), equalTo("Name"));
        assertThat(table.row(0).column(1).paragraph(0).text(), equalTo("Value"));
        assertThat(table.row(1).column(0).paragraph(0).text(), equalTo("Full Name"));
        assertThat(table.row(1).column(1).paragraph(0).text(), equalTo("Jean-Luc Picard"));
        assertThat(table.row(2).column(0).paragraph(0).text(), equalTo("Birthdate"));
        assertThat(table.row(2).column(1).paragraph(0).text(), equalTo(birthdate));
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
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = XWPFDocumentWrapper.parse(xwpfDocument);
        assertThat(documentWrapper.paragraph(0).text(), equalTo("Captain: Jean-Luc Picard"));
        assertThat(documentWrapper.paragraph(2).text(), equalTo("First Officer"));
        assertThat(documentWrapper.table(0).row(0).column(0).paragraph(0).text(), equalTo("Name"));
        assertThat(documentWrapper.table(0).row(0).column(1).paragraph(0).text(), equalTo("Rank"));
        assertThat(documentWrapper.table(0).row(0).column(2).paragraph(0).text(), equalTo("Uniform"));
        assertThat(documentWrapper.table(0).row(1).column(0).paragraph(0).text(), equalTo("Riker"));
        assertThat(documentWrapper.table(0).row(1).column(1).paragraph(0).text(), equalTo("3"));
        assertThat(documentWrapper.table(0).row(1).column(2).paragraph(0).text(), equalTo("Red"));
        assertThat(documentWrapper.paragraph(5).text(), equalTo("Services"));
        assertThat(documentWrapper.paragraph(7).text(), equalTo("USS Enterprise"));
        assertThat(documentWrapper.paragraph(8).text(), equalTo("US Defiant"));
        assertThat(documentWrapper.paragraph(10).text(), equalTo("And that’s that."));
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
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = XWPFDocumentWrapper.parse(xwpfDocument);
        assertThat(documentWrapper.paragraph(2).run(0).pictures().size(), equalTo(1));
    }
}