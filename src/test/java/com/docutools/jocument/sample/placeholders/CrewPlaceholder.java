package com.docutools.jocument.sample.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;

public class CrewPlaceholder implements PlaceholderData {
  private static final Logger logger = LogManager.getLogger();

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public void transform(Object placeholder, ExcelWriter excelWriter, Locale locale, GenerationOptions options) {
    if (!(placeholder instanceof Row row)) {
      logger.error("{} is not an instance of Row", placeholder);
      throw new IllegalArgumentException("Only Row accepted.");
    }
    transform(row, excelWriter);
  }

  private void transform(Row row, ExcelWriter excelWriter) {
    excelWriter.newRow(row);
    excelWriter.addCell(row.getCell(0), 5.0);
  }
}
