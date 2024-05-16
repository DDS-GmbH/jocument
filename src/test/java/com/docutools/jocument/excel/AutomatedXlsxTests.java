package com.docutools.jocument.excel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.docutools.jocument.Document;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.TestUtils;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.poipath.xssf.XSSFWorkbookWrapper;
import java.io.IOException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("Excel Generator Tests")
@Tag("automated")
@Tag("xssf")
class AutomatedXlsxTests {

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
}
