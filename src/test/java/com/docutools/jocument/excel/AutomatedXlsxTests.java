package com.docutools.jocument.excel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.docutools.jocument.Document;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.TestUtils;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.poipath.xssf.XSSFWorkbookWrapper;
import com.docutools.poipath.xwpf.XWPFDocumentWrapper;
import java.awt.Desktop;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("Word Generator Tests")
@Tag("automated")
@Tag("xwpf")
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
}
