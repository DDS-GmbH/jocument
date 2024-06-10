package com.docutools.jocument.sample.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.excel.interfaces.ExcelPlaceholderData;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.util.ModificationInformation;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;

public class CrewPlaceholder implements ExcelPlaceholderData {

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public ModificationInformation transform(Cell cell, ExcelWriter excelWriter, int offset, Locale locale, GenerationOptions options) {
    transform(cell, excelWriter, offset);
    return ModificationInformation.empty();
  }

  private void transform(Cell cell, ExcelWriter excelWriter, int offset) {
    excelWriter.addCell(cell, 5.0, offset);
  }
}
