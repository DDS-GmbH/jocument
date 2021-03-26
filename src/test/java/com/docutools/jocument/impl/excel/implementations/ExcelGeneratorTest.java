package com.docutools.jocument.impl.excel.implementations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


import com.docutools.jocument.Document;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.TestUtils;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.poipath.xssf.XSSFWorkbookWrapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


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
        var firstSheet = new XSSFWorkbookWrapper(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).text(), equalTo("This is the rythm of the night"));
        assertThat(firstSheet.row(1).cell(1).text(), equalTo("The night"));
        assertThat(firstSheet.row(2).cell(2).text(), equalTo("Ooh"));
        assertThat(firstSheet.row(3).cell(3).text(), equalTo("Oh yeah"));
        assertThat(firstSheet.row(4).cell(4).text(), equalTo("1312.0"));
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
        var firstSheet = new XSSFWorkbookWrapper(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).text(), equalTo("This is the rythm of the night"));
        assertThat(firstSheet.row(1).cell(1).text(), equalTo("The night"));
        assertThat(firstSheet.row(2).cell(2).text(), equalTo("Ooh"));
        assertThat(firstSheet.row(3).cell(3).text(), equalTo("Oh yeah"));
        assertThat(firstSheet.row(4).cell(4).doubleValue(), closeTo(1312.0, 0.1));
        assertThat(firstSheet.row(10).cell(5).text(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(11).cell(5).text(), equalTo("US Defiant"));
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
        workbook = TestUtils.getXSSFWorkbookFromDocument(document);
        var firstSheet = new XSSFWorkbookWrapper(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(1).text(), equalTo("Jean-Luc Picard"));
        assertThat(firstSheet.row(5).cell(0).text(), equalTo("Riker"));
        assertThat(firstSheet.row(9).cell(1).text(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(10).cell(1).text(), equalTo("US Defiant"));
        assertThat(firstSheet.row(12).cell(0).text(), equalTo("And that’s that"));
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
        var firstSheet = new XSSFWorkbookWrapper(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).text(), equalTo("User Profile"));
        assertThat(firstSheet.row(0).cell(1).text(), equalTo("Jean-Luc Picard"));
        assertThat(firstSheet.row(2).cell(0).text(), equalTo("Name"));
        assertThat(firstSheet.row(2).cell(1).text(), equalTo("Jean-Luc"));
        assertThat(firstSheet.row(3).cell(0).text(), equalTo("Last Name"));
        assertThat(firstSheet.row(3).cell(1).text(), equalTo("Picard"));
        assertThat(firstSheet.row(4).cell(0).text(), equalTo("Age"));
        assertThat(firstSheet.row(4).cell(1).text(), equalTo(String.valueOf(Period.between(LocalDate.of(1948, 9, 23), LocalDate.now()).getYears())));
        assertThat(firstSheet.row(4).cell(2).text(), equalTo(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US).format(LocalDate.of(1948, 9, 23))));
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
        var firstSheet = new XSSFWorkbookWrapper(workbook).sheet(0);
        assertThat(firstSheet.row(0).cell(0).doubleValue(), closeTo(1.0, 0.1));
        assertThat(firstSheet.row(1).cell(0).doubleValue(), closeTo(2.0, 0.1));
        assertThat(firstSheet.row(2).cell(0).doubleValue(), closeTo(3.0, 0.1));
        assertThat(firstSheet.row(3).cell(0).doubleValue(), closeTo(4.0, 0.1));
        assertThat(firstSheet.row(4).cell(0).doubleValue(), closeTo(5.0, 0.1));
        assertThat(firstSheet.row(7).cell(0).text(), equalTo("SUM(A1:A5)"));
        assertThat(firstSheet.row(7).cell(1).text(), equalTo("COUNT(A1:A5)"));
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
        var firstSheet = new XSSFWorkbookWrapper(workbook).sheet(0);
        assertThat(firstSheet.row(22).cell(1).text(), equalTo("USS Enterprise"));
        assertThat(firstSheet.row(23).cell(1).text(), equalTo("Mars"));
        assertThat(firstSheet.row(24).cell(1).text(), equalTo("Nova Rojava"));
        assertThat(firstSheet.row(26).cell(1).text(), equalTo("Nova Rojava"));
        assertThat(firstSheet.row(41).cell(1).text(), equalTo("Exarcheia"));
        assertThat(firstSheet.row(42).cell(1).text(), equalTo("Nova Metalkova"));
        assertThat(firstSheet.row(52).cell(0).text(), startsWith("Das Denken"));
    }
}