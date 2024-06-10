package com.docutools.jocument.sample.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.excel.interfaces.CellPlaceholderData;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;

public class CrewPlaceholder implements CellPlaceholderData {

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public void transform(Cell cell, ExcelWriter excelWriter, Locale locale, GenerationOptions options) {
    transform(cell, excelWriter);
  }

  private void transform(Cell cell, ExcelWriter excelWriter) {
    excelWriter.addCell(cell, 5.0);
  }
}
