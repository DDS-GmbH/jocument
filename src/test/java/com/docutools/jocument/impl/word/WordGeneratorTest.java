package com.docutools.jocument.impl.word;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.Document;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.TestUtils;
import com.docutools.jocument.impl.CustomPlaceholderRegistryImpl;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.jocument.sample.placeholders.QuotePlaceholder;
import com.docutools.poipath.xwpf.XWPFDocumentWrapper;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), equalTo("User Profile: Jean-Luc Picard"));
        assertThat(documentWrapper.bodyElement(2).asParagraph().text(), equalTo("Name: Jean-Luc"));
        assertThat(documentWrapper.bodyElement(3).asParagraph().text(), equalTo("Last Name: Picard"));
        assertThat(documentWrapper.bodyElement(4).asParagraph().text(), equalTo("Age: "
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
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), equalTo("User Profile: Jean-Luc Picard"));
        assertThat(documentWrapper.bodyElement(2).asParagraph().text(), equalTo("Name: Jean-Luc"));
        assertThat(documentWrapper.bodyElement(3).asParagraph().text(), equalTo("Last Name: Picard"));
        assertThat(documentWrapper.bodyElement(4).asParagraph().text(), equalTo("Age German: "
            + Period.between(LocalDate.of(1948, 9, 23), LocalDate.now()).getYears()
            + " ("
            + DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23))
            + ")"));
        assertThat(documentWrapper.bodyElement(5).asParagraph().text(), equalTo("Age English: "
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
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        var table = documentWrapper.bodyElement(0).asTable();
        var birthdate = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23));
        assertThat(table.row(0).cell(0).bodyElement(0).asParagraph().text(), equalTo("Name"));
        assertThat(table.row(0).cell(1).bodyElement(0).asParagraph().text(), equalTo("Value"));
        assertThat(table.row(1).cell(0).bodyElement(0).asParagraph().text(), equalTo("Full Name"));
        assertThat(table.row(1).cell(1).bodyElement(0).asParagraph().text(), equalTo("Jean-Luc Picard"));
        assertThat(table.row(2).cell(0).bodyElement(0).asParagraph().text(), equalTo("Birthdate"));
        assertThat(table.row(2).cell(1).bodyElement(0).asParagraph().text(), equalTo(birthdate));
    }

    @Test
    @Disabled("Pending apache poi 5.2.4 release")
    @DisplayName("Replace custom placeholders in tables.")
    void shouldReplaceCustomPlaceholderInTable() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/word/CustomPlaceholderInTableTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        var table = documentWrapper.bodyElement(0).asTable();
        assertThat(table.row(0).cell(1).bodyElement(0).asParagraph().run(0).pictures().size(), equalTo(1));
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
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), equalTo("Captain: Jean-Luc Picard"));
        assertThat(documentWrapper.bodyElement(2).asParagraph().text(), equalTo("First Officer"));
        assertThat(documentWrapper.bodyElement(3).asTable().row(0).cell(0).bodyElement(0).asParagraph().text(), equalTo("Name"));
        assertThat(documentWrapper.bodyElement(3).asTable().row(0).cell(1).bodyElement(0).asParagraph().text(), equalTo("Rank"));
        assertThat(documentWrapper.bodyElement(3).asTable().row(0).cell(2).bodyElement(0).asParagraph().text(), equalTo("Uniform"));
        assertThat(documentWrapper.bodyElement(3).asTable().row(1).cell(0).bodyElement(0).asParagraph().text(), equalTo("Riker"));
        assertThat(documentWrapper.bodyElement(3).asTable().row(1).cell(1).bodyElement(0).asParagraph().text(), equalTo("3"));
        assertThat(documentWrapper.bodyElement(3).asTable().row(1).cell(2).bodyElement(0).asParagraph().text(), equalTo("Red"));
        assertThat(documentWrapper.bodyElement(6).asParagraph().text(), equalTo("Services"));
        assertThat(documentWrapper.bodyElement(8).asParagraph().text(), equalTo("USS Enterprise"));
        assertThat(documentWrapper.bodyElement(9).asParagraph().text(), equalTo("US Defiant"));
        assertThat(documentWrapper.bodyElement(11).asParagraph().text(), equalTo("And that’s that."));
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
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(2).asParagraph().run(0).pictures().size(), equalTo(1));
    }

    @Test
    @DisplayName("Apply foreign custom word placeholder.")
    void shouldApplyForeignCustomWordPlaceholder() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/word/QuoteTemplate.docx")
            .orElseThrow();
        CustomPlaceholderRegistry customPlaceholderRegistry = new CustomPlaceholderRegistryImpl();
        customPlaceholderRegistry.addHandler("quote", QuotePlaceholder.class);
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD, customPlaceholderRegistry);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().run(0).text(),
            equalTo("Live your life not celebrating victories, but overcoming defeats."));
    }

    @Test
    @DisplayName("Resolve legacy placeholder")
    void shouldResolveLegacy() throws IOException, InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/LegacyCollectionsTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), equalTo("Captain: Jean-Luc Picard"));
        assertThat(documentWrapper.bodyElement(1).asParagraph().text(), equalTo("Riker"));
        assertThat(documentWrapper.bodyElement(3).asParagraph().text(), equalTo("First Officer"));
        assertThat(documentWrapper.bodyElement(4).asTable().row(0).cell(0).bodyElement(0).asParagraph().text(), equalTo("Name"));
        assertThat(documentWrapper.bodyElement(4).asTable().row(0).cell(1).bodyElement(0).asParagraph().text(), equalTo("Rank"));
        assertThat(documentWrapper.bodyElement(4).asTable().row(0).cell(2).bodyElement(0).asParagraph().text(), equalTo("Uniform"));
        assertThat(documentWrapper.bodyElement(4).asTable().row(1).cell(0).bodyElement(0).asParagraph().text(), equalTo("Riker"));
        assertThat(documentWrapper.bodyElement(4).asTable().row(1).cell(1).bodyElement(0).asParagraph().text(), equalTo("3"));
        assertThat(documentWrapper.bodyElement(4).asTable().row(1).cell(2).bodyElement(0).asParagraph().text(), equalTo("Red"));
        assertThat(documentWrapper.bodyElement(7).asParagraph().text(), equalTo("Services"));
        assertThat(documentWrapper.bodyElement(9).asParagraph().text(), equalTo("USS Enterprise"));
        assertThat(documentWrapper.bodyElement(10).asParagraph().text(), equalTo("US Defiant"));
        assertThat(documentWrapper.bodyElement(12).asParagraph().text(), equalTo("And that’s that."));
    }

    @Test
    @DisplayName("Resolve future placeholder")
    void shouldResolveFuture() throws IOException, InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/CollectionsTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.FUTURE_PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), equalTo("Captain: Jean-Luc Picard"));
        assertThat(documentWrapper.bodyElement(2).asParagraph().text(), equalTo("First Officer"));
        var table = documentWrapper.bodyElement(3).asTable();
        assertThat(table.row(0).cell(0).bodyElement(0).asParagraph().text(), equalTo("Name"));
        assertThat(table.row(0).cell(1).bodyElement(0).asParagraph().text(), equalTo("Rank"));
        assertThat(table.row(0).cell(2).bodyElement(0).asParagraph().text(), equalTo("Uniform"));
        assertThat(table.row(1).cell(0).bodyElement(0).asParagraph().text(), equalTo("Riker"));
        assertThat(table.row(1).cell(1).bodyElement(0).asParagraph().text(), equalTo("3"));
        assertThat(table.row(1).cell(2).bodyElement(0).asParagraph().text(), equalTo("Red"));
        assertThat(documentWrapper.bodyElement(6).asParagraph().text(), equalTo("Services"));
        assertThat(documentWrapper.bodyElement(8).asParagraph().text(), equalTo("USS Enterprise"));
        assertThat(documentWrapper.bodyElement(9).asParagraph().text(), equalTo("US Defiant"));
        assertThat(documentWrapper.bodyElement(11).asParagraph().text(), equalTo("And that’s that."));
    }

    @Test
    @DisplayName("Resolve in scoped mode")
    void shouldResolveInScopedMode() throws IOException, InterruptedException {
        // Assemble
        var template = Template.fromClassPath("/templates/word/ScopedTemplate.docx")
            .orElseThrow();
        var resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        var table = documentWrapper.bodyElement(0).asTable();
        assertThat(table.row(0).cell(0).bodyElement(0).asParagraph().text(), equalTo("Ship"));
        assertThat(table.row(0).cell(1).bodyElement(0).asParagraph().text(), equalTo("Crew"));
        assertThat(table.row(0).cell(2).bodyElement(0).asParagraph().text(), equalTo("Captain"));
        assertThat(table.row(0).cell(3).bodyElement(0).asParagraph().text(), equalTo("Officer"));
        assertThat(table.row(1).cell(0).bodyElement(0).asParagraph().text(), equalTo(SampleModelData.ENTERPRISE.name()));
        assertThat(table.row(1).cell(1).bodyElement(0).asParagraph().text(), equalTo(String.valueOf(SampleModelData.ENTERPRISE.crew())));
        assertThat(table.row(1).cell(2).bodyElement(0).asParagraph().text(), equalTo(SampleModelData.ENTERPRISE.captain().getName()));
        assertThat(table.row(1).cell(3).bodyElement(0).asParagraph().text(), equalTo(SampleModelData.ENTERPRISE.captain().getOfficer().getName()));
    }

    @Test
    @DisplayName("Resolve truthy conditional")
    void shouldResolveTruthyConditional() throws IOException, InterruptedException {
        // Assemble
        var template = Template.fromClassPath("/templates/word/ConditionalTemplate.docx")
            .orElseThrow();
        var resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        var services = SampleModelData.ENTERPRISE.services();
        var servicesOnePlanets = services.get(0).getVisitedPlanets();
        var servicesTwoPlanets = services.get(1).getVisitedPlanets();
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), equalTo(servicesOnePlanets.get(0).getPlanetName()));
        assertThat(documentWrapper.bodyElement(1).asParagraph().text(), equalTo(servicesTwoPlanets.get(0).getPlanetName()));
        assertThat(documentWrapper.bodyElement(2).asParagraph().text(), equalTo(servicesTwoPlanets.get(1).getPlanetName()));
    }

    @Test
    @DisplayName("Resolve falsy conditional")
    void shouldResolveFalsyConditional() throws IOException, InterruptedException {
        // Assemble
        var template = Template.fromClassPath("/templates/word/ConditionalTemplate.docx")
            .orElseThrow();
        var resolver = new ReflectionResolver(SampleModelData.ENTERPRISE_WITHOUT_SERVICES);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.document().getBodyElements(), hasSize(0));
    }

    @Test
    @DisplayName("Scale large picture")
    void shouldScaleLargePicture() throws IOException, InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/ProfilePicTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.FUTURE_PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().run(0).pictures(), hasSize(1));
    }

    @Test
    @DisplayName("Process Document With TOC")
    void shouldProcessDocumentWithTOC() throws IOException, InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/TOCTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.FUTURE_PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().runs(), hasSize(1));
    }

    @Test
    @DisplayName("Placeholder in Header")
    void shouldReplacePlaceholderInHeader() throws IOException, InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/HeaderTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var headerList = xwpfDocument.getHeaderList();
        assertThat(headerList.get(0).getText(), containsString("Jean-Luc Picard"));
        assertThat(headerList.get(0).getText(), containsString("23.09.1948"));
    }

    @Test
    @DisplayName("Placeholder in Footer")
    void shouldReplacePlaceholderInFooter() throws IOException, InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/FooterTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var footerList = xwpfDocument.getFooterList();
        assertThat(footerList.get(0).getText(), containsString("Jean-Luc Picard"));
        assertThat(footerList.get(0).getText(), containsString("23.09.1948"));
    }

    @Test
    @DisplayName("Formats Instant with Generation Options")
    void shouldFormatInstant() throws IOException, InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/InstantTemplate.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);
        GenerationOptions generationOptions = new GenerationOptionsBuilder()
            .format(Instant.class, (locale, instant) -> instant.toString())
            .build();
        resolver.setOptions(generationOptions);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), containsString(SampleModelData.PICARD_PERSON.getEntryDate().toString()));
    }

    @Test
    @DisplayName("Resolve IterablePlaceholder with toString when there's no closing placeholder.")
    void shouldResolveIPWithToStringWhenNoLoop() throws InterruptedException, IOException {
        // Assemble
        Template template = Template.fromClassPath("/templates/word/IterablePlaceholderWithoutLoop.docx")
            .orElseThrow();
         var resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().text(), containsString(SampleModelData.PICARD.getOfficer().toString()));
    }

    @Test
    @DisplayName("Resolve custom placeholder in custom placeholderdata")
    void shouldResolveCustomPlaceholderInCustomPlaceholderData() throws InterruptedException, IOException {
        // assemble
        Template template = Template.fromClassPath("/templates/word/CustomPlaceholderInCustomData.docx")
            .orElseThrow();
        CustomPlaceholderRegistry customPlaceholderRegistry = new CustomPlaceholderRegistryImpl();
        customPlaceholderRegistry.addHandler("quote", QuotePlaceholder.class);
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PLANET, customPlaceholderRegistry);

        // act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().run(0).text(),
            equalTo("Live your life not celebrating victories, but overcoming defeats."));
    }

    @Test
    void dynamicAccess() throws InterruptedException, IOException {
        // assemble
        Template template = Template.fromClassPath("/templates/word/DynamicAccess.docx")
            .orElseThrow();
        SampleModelData.PICARD_PERSON.setFavouriteShip(SampleModelData.ENTERPRISE);
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

        // act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        var documentWrapper = new XWPFDocumentWrapper(xwpfDocument);
        assertThat(documentWrapper.bodyElement(0).asParagraph().run(0).text(), equalTo(SampleModelData.ENTERPRISE.name()));
    }

    @Test
    void pictureInHeader() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/word/PictureInHeader.docx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        xwpfDocument = TestUtils.getXWPFDocumentFromDocument(document);
        assertThat(xwpfDocument.getHeaderArray(0).getAllPictures(), hasSize(1));
    }
}