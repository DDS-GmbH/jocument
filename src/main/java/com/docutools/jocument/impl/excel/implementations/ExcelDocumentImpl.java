package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * This class is responsible for setting up the necessary streams for generating excel document from templates
 * and for passing each sheet of a workbook to the excel generator.
 *
 * @author Anton Oellerer
 * @since 2020-04-07
 */
public class ExcelDocumentImpl extends DocumentImpl {
  private static final Logger logger = LogManager.getLogger();

  /**
   * The constructor for a new ExcelDocument report generator.
   *
   * @param template The template to generate the report from
   * @param resolver The resolver to use for filling placeholders
   */
  public ExcelDocumentImpl(Template template, PlaceholderResolver resolver, GenerationOptions options) {
    super(template, resolver, options);
  }

  /**
   * Start generation of an Excel report from the template supplied in the constructor, using the also supplied
   * resolver for resolving placeholders.
   *
   * @return The path to the generated report
   * @throws IOException If reading of the template or writing of the report fails.
   */
  @Override
  protected Path generate() throws IOException {
    logger.info("Starting generation");
    Path file = Files.createTempFile("jocument-", ".xlsx");
    try (XSSFWorkbook workbook = new XSSFWorkbook(template.openStream())) {
      ExcelWriter excelWriter = new XSSFWriter(workbook);
      for (Iterator<Sheet> it = workbook.sheetIterator(); it.hasNext(); ) {
        Sheet sheet = it.next();
        sanitizeSheet(sheet);
        excelWriter.newSheet(sheet);
        logger.info("Starting generation of sheet {}", sheet.getSheetName());
        ExcelGenerator.apply(resolver, StreamSupport.stream(sheet.spliterator(), false).toList(), excelWriter, options);
      }
      XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
      try (OutputStream os = Files.newOutputStream(file)) {
        logger.info("Writing document to {}", os);
        workbook.write(os);
      }
    }
    return file;
  }

  private void sanitizeSheet(Sheet sheet) {
    // creates rows where there are none
    int lastRowNum = sheet.getLastRowNum();

    for (int i = 0; i <= lastRowNum; i++) {
      var row = sheet.getRow(i);
      if (row == null) {
        sheet.createRow(i);
      }
    }
  }
}
