package com.docutools.jocument.sample.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.excel.interfaces.ExcelPlaceholderData;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.util.ModificationInformation;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class QuotesBlockPlaceholder implements ExcelPlaceholderData {
  public static final Map<String, String> quotes = Map.of("marx", "From each according to his abilities, to each according to his needs.",
      "engels", "An ounce of action is worth a ton of theory.", "lenin", "A lie told often enough becomes the truth.");

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public ModificationInformation transform(Cell cell, ExcelWriter excelWriter, int offset, Locale locale, GenerationOptions options) {
    return transform(cell.getRow(), excelWriter);
  }

  private ModificationInformation transform(Row row, ExcelWriter excelWriter) {
    int cellPointer = getPlaceholderStart(row);
    while (!row.getCell(cellPointer).getStringCellValue().equals("{{/quotes}}")) {
      Cell authorCell = row.getCell(cellPointer + 1);  //cells get shifted to the left by one since the placeholder is removed
      String quote = quotes.get(authorCell.getStringCellValue().toLowerCase());
      excelWriter.addCell(authorCell, quote, -1);
      cellPointer++;
      if (cellPointer > row.getPhysicalNumberOfCells()) {
        throw new RuntimeException("Row %s did not contain {{/quotes}} placeholder".formatted(row.getRowNum()));
      }
    }
    return new ModificationInformation(Optional.of(cellPointer), -2);
  }

  private int getPlaceholderStart(Row row) {
    int pointer = 0;
    while (!row.getCell(pointer).getStringCellValue().equals("{{quotes}}")) {
      pointer++;
      if (pointer > row.getPhysicalNumberOfCells()) {
        throw new RuntimeException("Row %s did not contain {{quotes}} placeholder".formatted(row.getRowNum()));
      }
    }
    return pointer;
  }
}
