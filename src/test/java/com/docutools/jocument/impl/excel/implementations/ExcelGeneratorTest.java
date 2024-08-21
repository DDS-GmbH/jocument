package com.docutools.jocument.impl.excel.implementations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.Document;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.MimeType;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.TestUtils;
import com.docutools.jocument.impl.CustomPlaceholderRegistryImpl;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.jocument.sample.placeholders.CrewPlaceholder;
import com.docutools.jocument.sample.placeholders.QuotesBlockPlaceholder;
import com.docutools.poipath.PoiPath;
import com.docutools.poipath.xssf.RowWrapper;
import com.docutools.poipath.xssf.XSSFWorkbookWrapper;
import java.awt.Desktop;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;


@DisplayName("Excel Generator Tests")
@Tag("automated")
@Tag("xssf")
class ExcelGeneratorTest {
    XSSFWorkbook workbook;

    @AfterEach
    void cleanup() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }

    @Test
    @DisplayName("Should copy constant cell values into new document.")
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
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo("This is the rythm of the night"));
        assertThat(firstSheet.row(1).cell(1).stringValue(), equalTo("The night"));
        assertThat(firstSheet.row(2).cell(2).stringValue(), equalTo("Ooh"));
        assertThat(firstSheet.row(3).cell(3).stringValue(), equalTo("Oh yeah"));
        assertThat(firstSheet.row(4).cell(4).text(), equalTo("1312.0"));
    }

    @Test
    void simpleLoop() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/SimpleLoop.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(1).cell(0).stringValue(), equalTo("US Defiant"));
    }

    @Test
    void simpleLoopWithSpacingAtStart() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/SimpleLoopWithSpacingAtStart.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(1).cell(0).stringValue(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(3).cell(0).stringValue(), equalTo("US Defiant"));
    }

    @Test
    void simpleLoopWithSpacingAtEnd() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/SimpleLoopWithSpacingAtEnd.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(2).cell(0).stringValue(), equalTo("US Defiant"));
    }

    @Test
    void nestedSimpleLoop() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/NestedSimpleLoop.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        var services = SampleModelData.PICARD.getServices();
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo(services.get(0).getVisitedPlanets().get(0).getPlanetName()));
        assertThat(firstSheet.row(1).cell(0).stringValue(), equalTo(services.get(1).getVisitedPlanets().get(0).getPlanetName()));
    }


    @Test
    void multiValueLoop() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/MultiValueLoop.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        var services = SampleModelData.PICARD.getServices();
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo(services.get(0).getShipName()));
        assertThat(firstSheet.row(1).cell(0).stringValue(), equalTo(services.get(0).getShipName()));
        assertThat(firstSheet.row(2).cell(0).stringValue(), equalTo(services.get(1).getShipName()));
        assertThat(firstSheet.row(3).cell(0).stringValue(), equalTo(services.get(1).getShipName()));
    }


    @Test
    void multiValueLoopWithSpacingInBetween() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/MultiValueLoopWithSpacingInBetween.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        var services = SampleModelData.PICARD.getServices();
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo(services.get(0).getShipName()));
        assertThat(firstSheet.row(2).cell(0).stringValue(), equalTo(services.get(0).getShipName()));
        assertThat(firstSheet.row(3).cell(0).stringValue(), equalTo(services.get(1).getShipName()));
        assertThat(firstSheet.row(5).cell(0).stringValue(), equalTo(services.get(1).getShipName()));
    }

    @Test
    void nestedMultiValueLoop() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/NestedMultiValueLoop.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        var services = SampleModelData.PICARD.getServices();
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo(services.get(0).getVisitedPlanets().get(0).getPlanetName()));
        assertThat(firstSheet.row(1).cell(0).stringValue(), equalTo(services.get(0).getVisitedPlanets().get(0).getPlanetName()));
        assertThat(firstSheet.row(2).cell(0).stringValue(), equalTo(services.get(1).getVisitedPlanets().get(0).getPlanetName()));
        assertThat(firstSheet.row(3).cell(0).stringValue(), equalTo(services.get(1).getVisitedPlanets().get(1).getPlanetName()));
    }

    @Test
    @DisplayName("Should copy constant cell values in a loop.")
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
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo("This is the rythm of the night"));
        assertThat(firstSheet.row(1).cell(1).stringValue(), equalTo("The night"));
        assertThat(firstSheet.row(2).cell(2).stringValue(), equalTo("Ooh"));
        assertThat(firstSheet.row(3).cell(3).stringValue(), equalTo("Oh yeah"));
        assertThat(firstSheet.row(4).cell(4).doubleValue(), closeTo(1312.0, 0.1));
        assertThat(firstSheet.row(10).cell(5).stringValue(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(11).cell(5).stringValue(), equalTo("US Defiant"));
    }

    @Test
    @DisplayName("Should resolve placeholder values in Excel in loop.")
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
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(1).stringValue(), equalTo("Jean-Luc Picard"));
        assertThat(firstSheet.row(5).cell(0).stringValue(), equalTo("Riker"));
        assertThat(firstSheet.row(9).cell(1).stringValue(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(10).cell(1).stringValue(), equalTo("US Defiant"));
        assertThat(firstSheet.row(12).cell(0).stringValue(), equalTo("And that’s that"));
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
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo("User Profile"));
        assertThat(firstSheet.row(0).cell(1).stringValue(), equalTo("Jean-Luc Picard"));
        assertThat(firstSheet.row(2).cell(0).stringValue(), equalTo("Name"));
        assertThat(firstSheet.row(2).cell(1).stringValue(), equalTo("Jean-Luc"));
        assertThat(firstSheet.row(3).cell(0).stringValue(), equalTo("Last Name"));
        assertThat(firstSheet.row(3).cell(1).stringValue(), equalTo("Picard"));
        assertThat(firstSheet.row(4).cell(0).stringValue(), equalTo("Age"));
        assertThat(firstSheet.row(4).cell(1).doubleValue(), equalTo((double) Period.between(LocalDate.of(1948, 9, 23), LocalDate.now()).getYears()));
        assertThat(firstSheet.row(4).cell(2).stringValue(), equalTo(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23))));
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
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).doubleValue(), closeTo(1.0, 0.1));
        assertThat(firstSheet.row(1).cell(0).doubleValue(), closeTo(2.0, 0.1));
        assertThat(firstSheet.row(2).cell(0).doubleValue(), closeTo(3.0, 0.1));
        assertThat(firstSheet.row(3).cell(0).doubleValue(), closeTo(4.0, 0.1));
        assertThat(firstSheet.row(4).cell(0).doubleValue(), closeTo(5.0, 0.1));
        assertThat(firstSheet.row(7).cell(0).text(), equalTo("SUM(A1:A5)"));
        assertThat(firstSheet.row(7).cell(0).intValue(), equalTo(15));
        assertThat(firstSheet.row(7).cell(1).text(), equalTo("COUNT(A1:A5)"));
        assertThat(firstSheet.row(7).cell(1).intValue(), equalTo(5));
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
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(22).cell(1).stringValue(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(23).cell(1).stringValue(), equalTo("Mars"));
        assertThat(firstSheet.row(24).cell(1).stringValue(), equalTo("Nova Rojava"));
        assertThat(firstSheet.row(26).cell(1).stringValue(), equalTo("Nova Rojava"));
        assertThat(firstSheet.row(41).cell(1).stringValue(), equalTo("Exarcheia"));
        assertThat(firstSheet.row(42).cell(1).stringValue(), equalTo("Nova Metalkova"));
        assertThat(firstSheet.row(52).cell(0).stringValue(), startsWith("Das Denken"));
    }

    @Test
    void shouldResolveHyperlink() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/HyperlinkDocument.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var documentWrapper = new XSSFWorkbookWrapper(xssfWorkbook);
        assertThat(documentWrapper.sheet(0).row(0).cell(0).text(), equalTo("orf.at"));
        assertThat(documentWrapper.sheet(0).row(0).cell(0).cell().getHyperlink().getAddress(), equalTo("https://orf.at/"));
    }

    @Test
    void shouldResolveHyperlinkFormula() throws InterruptedException, IOException {
      // Arrange
      Template template = Template.fromClassPath("/templates/excel/HyperlinkFormula.xlsx")
          .orElseThrow();
      PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

      // Act
      Document document = template.startGeneration(resolver);
      document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        var workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).cell().getCellType(), equalTo(CellType.FORMULA));
        assertThat(firstSheet.row(0).cell(0).cell().getCellFormula(),
            equalTo("HYPERLINK(\"https://link.me/USS Enterprise\", \"USS Enterprise\")"));
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo("USS Enterprise"));
    }

    @Test
    void xlsxQuotesBlockPlaceholder() throws InterruptedException, IOException {
        Template template = Template.fromClassPath("/templates/excel/QuotesBlockTemplate.xlsx")
            .orElseThrow();
        CustomPlaceholderRegistry customPlaceholderRegistry = new CustomPlaceholderRegistryImpl();
        customPlaceholderRegistry.addHandler("quotes", QuotesBlockPlaceholder.class);
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD, customPlaceholderRegistry);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        var workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = PoiPath.xssf(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).stringValue(), equalTo(QuotesBlockPlaceholder.quotes.get("marx")));
        assertThat(firstSheet.row(0).cell(1).stringValue(), equalTo(QuotesBlockPlaceholder.quotes.get("engels")));
    }

    @Test
    @Disabled("This test is disabled because it takes too long to run.")
    void evaluatesLargeDocument() throws InterruptedException {
        // Assemble
        Template template = Template.fromClassPath("/templates/excel/LargeTemplate.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));

    }

    @Test
    @DisplayName("Resolve IterablePlaceholder with toString when there's no closing placeholder.")
    void shouldResolveIPWithToStringWhenNoLoop() throws InterruptedException, IOException {
        // Assemble
        Template template = Template.fromClassPath("/templates/excel/IterablePlaceholderWithoutLoop.xlsx")
            .orElseThrow();
        var resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var documentWrapper = new XSSFWorkbookWrapper(workbook);
        assertThat(documentWrapper.sheet(0).row(0).cell(0).text(), containsString(SampleModelData.PICARD.getOfficer().toString()));
    }


    @Test
    void insertNumericValue() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/NumericValues.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(5_000L); // 5 seconds

        // Assert
        assertThat(document.completed(), is(true));
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var xssf = new XSSFWorkbookWrapper(xssfWorkbook);
        var sheet = xssf.sheet(0);
        assertThat(sheet.row(0).cell(0).doubleValue(), is(5.0));
    }


    @Test
    void insertNumericValueFromCustomPlaceholder() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/NumericValues.xlsx")
            .orElseThrow();
        CustomPlaceholderRegistry customPlaceholderRegistry = new CustomPlaceholderRegistryImpl() {
            @Override
            public boolean governs(String placeholderName, Object bean, Optional<MimeType> mimeType) {
                if (placeholderName.equals("crew") && mimeType.isPresent()) {
                    return mimeType.get().equals(MimeType.XLSX);
                } else {
                    return governs(placeholderName, bean);
                }
            }
        };
        customPlaceholderRegistry.addHandler("crew", CrewPlaceholder.class);
        GenerationOptions generationOptions = new GenerationOptionsBuilder().withMimeType(MimeType.XLSX).build();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.ENTERPRISE, customPlaceholderRegistry, generationOptions);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(5_000L); // 5 seconds

        // Assert
        assertThat(document.completed(), is(true));
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var xssf = new XSSFWorkbookWrapper(xssfWorkbook);
        var sheet = xssf.sheet(0);
        assertThat(sheet.row(0).cell(0).doubleValue(), is(5.0));
    }

    @Test
    void multiplePlaceholdersPerRow() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/MultiplePlaceholdersPerRow.xlsx").orElseThrow();
        CustomPlaceholderRegistry customPlaceholderRegistry = new CustomPlaceholderRegistryImpl();
        customPlaceholderRegistry.addHandler("crew", CrewPlaceholder.class);
        GenerationOptions generationOptions = new GenerationOptionsBuilder().withMimeType(MimeType.XLSX).build();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.ENTERPRISE, customPlaceholderRegistry, generationOptions);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(5_000L); // 5 seconds

        // Assert
        assertThat(document.completed(), is(true));
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var xssf = new XSSFWorkbookWrapper(xssfWorkbook);
        var sheet = xssf.sheet(0);
        assertThat(sheet.row(0).cell(0).stringValue(), is(SampleModelData.ENTERPRISE.name()));
        assertThat(sheet.row(0).cell(1).cell().toString(), is("5.0"));
    }

    @Test
    void rangedRowPlaceholder() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/RangedRowPlaceholder.xlsx").orElseThrow();
        CustomPlaceholderRegistry customPlaceholderRegistry = new CustomPlaceholderRegistryImpl();
        customPlaceholderRegistry.addHandler("crew", CrewPlaceholder.class);
        customPlaceholderRegistry.addHandler("quotes", QuotesBlockPlaceholder.class);
        GenerationOptions generationOptions = new GenerationOptionsBuilder().withMimeType(MimeType.XLSX).build();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.ENTERPRISE, customPlaceholderRegistry, generationOptions);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(50_000L); // 5 seconds

        // Assert
        assertThat(document.completed(), is(true));
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var xssf = new XSSFWorkbookWrapper(xssfWorkbook);
        var sheet = xssf.sheet(0);
        RowWrapper row = sheet.row(0);
        assertThat(row.cell(0).stringValue(), equalTo(QuotesBlockPlaceholder.quotes.get("marx")));
        assertThat(row.cell(1).stringValue(), equalTo(QuotesBlockPlaceholder.quotes.get("engels")));
        assertThat(row.cell(2).cell().toString(), is("5.0"));
    }

    @Test
    void copiesHyperlink() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/HyperlinkDocument.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(60000L); // 1 minute

        // Assert
        assertThat(document.completed(), is(true));
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var documentWrapper = new XSSFWorkbookWrapper(xssfWorkbook);
        assertThat(documentWrapper.sheet(0).row(0).cell(0).text(), equalTo("orf.at"));
        assertThat(documentWrapper.sheet(0).row(0).cell(0).cell().getHyperlink().getAddress(), equalTo("https://orf.at/"));
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
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var xssf = new XSSFWorkbookWrapper(xssfWorkbook);
        var sheet = xssf.sheet(0);
        var autoFilter = sheet.sheet().getCTWorksheet().getAutoFilter();
        assertThat(autoFilter, notNullValue());
        var autoFilterRef = autoFilter.getRef();
        var rangeAddress = CellRangeAddress.valueOf(autoFilterRef);
        assertThat(rangeAddress.isInRange(sheet.row(0).cell(0).cell()), is(true));
    }

    @Test
    void copiesDiagram() throws InterruptedException, IOException {
        // Arrange
        Template template = Template.fromClassPath("/templates/excel/Diagrams.xlsx")
            .orElseThrow();
        PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.ARMY);

        // Act
        Document document = template.startGeneration(resolver);
        document.blockUntilCompletion(5_000L); // 5 seconds

        // Assert
        assertThat(document.completed(), is(true));
        var xssfWorkbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var xssf = new XSSFWorkbookWrapper(xssfWorkbook);
        assertThat(xssf.sheet(0).sheet().getDrawingPatriarch().getShapes().size(), equalTo(1));
    }
}