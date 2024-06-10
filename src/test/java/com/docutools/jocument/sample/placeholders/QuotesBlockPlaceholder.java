package com.docutools.jocument.sample.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.interfaces.RowPlaceholderData;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class QuotesBlockPlaceholder implements RowPlaceholderData {
  public static final Map<String, String> quotes = Map.of("marx", "From each according to his abilities, to each according to his needs.",
      "engels", "An ounce of action is worth a ton of theory.", "lenin", "A lie told often enough becomes the truth.");

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public void transform(Row row, ExcelWriter excelWriter, Locale locale, GenerationOptions options) {
    transform(row, excelWriter);
  }

  private void transform(Row row, ExcelWriter excelWriter) {
    int cellPointer = getPlaceholderStart(row);
    excelWriter.newRow(row);
    while (!row.getCell(cellPointer).getStringCellValue().equals("{{/quotes}}")) {
      Cell authorCell = row.getCell(cellPointer + 1);  //cells get shifted to the left by one since the placeholder is removed
      String quote = quotes.get(authorCell.getStringCellValue().toLowerCase());
      excelWriter.addCell(authorCell, quote, -1);
      cellPointer++;
      if (cellPointer > row.getPhysicalNumberOfCells()) {
        throw new RuntimeException("Row %s did not contain {{/quotes}} placeholder".formatted(row.getRowNum()));
      }
    }
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
