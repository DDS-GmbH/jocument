package com.docutools.jocument.impl.excel.util;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.ParsingUtils;
import java.util.Locale;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {

  private ExcelUtils() {
  }

  /**
   * Resolve a cell to a placeholderData.
   *
   * @param cellValue The cell content as a string
   * @param resolver The resolver to use for resolving
   * @return An empty {@link Optional} if the cellValue did not resolve to anything, otherwise an {@link Optional} containing the resolved content.
   */
  public static Optional<PlaceholderData> resolveCell(String cellValue, PlaceholderResolver resolver) {
    if (cellValue == null || !cellValue.startsWith("{{") && !cellValue.endsWith("}}")) {
      return Optional.empty();
    }
    var matchingResults = ParsingUtils.matchPlaceholders(cellValue).results().toList();
    if (matchingResults.size() != 1) {
      return Optional.empty();
    }
    return resolver.resolve(matchingResults.get(0).group(1));
  }

  public static String getPlaceholder(Cell cell) {
    return ParsingUtils.stripBrackets(cell.getStringCellValue());
  }

  public static String getPlaceholder(Row row) {
    return ParsingUtils.stripBrackets(row.getCell(row.getFirstCellNum()).getStringCellValue());
  }

  public static boolean isSimpleCell(Cell cell) {
    return cell.getCellType() != CellType.STRING
        || cell.getCellType() == CellType.STRING && !DocumentImpl.TAG_PATTERN.asPredicate().test(cell.getStringCellValue());
  }

  public static boolean containsPlaceholder(Cell cell) {
    String cellValue = getCellContentAsString(cell);
    return ParsingUtils.matchPlaceholders(cellValue).find();
  }

  /**
   * This method checks whether the handed row contains a loop-end cell.
   *
   * @param row The row to check for a loop end string
   * @return Whether the row contains a loop end string
   */
  public static boolean isLoopEnd(Row row) {
    if (row.getPhysicalNumberOfCells() == 1) {
      var cell = row.getCell(row.getFirstCellNum());
      if (cell.getCellType() == CellType.STRING) {
        return DocumentImpl.LOOP_END_PATTERN.asPredicate().test(cell.getStringCellValue());
      }
    }
    return false;
  }

  public static String getPlaceholderFromLoopEnd(Row row) {
    return ParsingUtils.stripBrackets(row.getCell(row.getFirstCellNum()).getStringCellValue()).substring(1);
  }

  /**
   * This method checks whether the passed row contains the loop-end tag for the passed placeholder.
   *
   * @param row         The row to check for the correct loop-end string
   * @param placeholder The loop-start placeholder for which we want to know whether it is terminated in this row
   * @return Whether the matching loop-end string for the placeholder was found in this row
   */
  public static boolean isMatchingLoopEnd(Row row, String placeholder) {
    var endPlaceholders = ParsingUtils.getMatchingLoopEnds(placeholder);
    if (getNumberOfNonEmptyCells(row) == 1) {
      var cell = row.getCell(row.getFirstCellNum());
      if (cell.getCellType() == CellType.STRING) {
        return endPlaceholders.stream().anyMatch(endPlaceholder -> cell.getStringCellValue().strip().toLowerCase().equals(endPlaceholder));
      }
    }
    return false;
  }

  /**
   * This method checks whether the passed row contains the loop-start tag for the passed placeholder.
   *
   * @param row         The row to check for the correct loop-start string
   * @param placeholder The loop-start placeholder for which we want to know whether it is started in this row
   * @return Whether the matching loop-start string for the placeholder was found in this row
   */
  public static boolean isMatchingLoopStart(Row row, String placeholder) {
    if (row.getPhysicalNumberOfCells() == 1) {
      var cell = row.getCell(row.getFirstCellNum());
      if (cell.getCellType() == CellType.STRING) {
        return ParsingUtils.stripBrackets(cell.getStringCellValue()).equals(placeholder);
      }
    }
    return false;
  }

  /**
   * Get the nuber of cells which are not empty in a row.
   *
   * @param row The row to check
   * @return The number of cells in the row which contain a value
   */
  public static long getNumberOfNonEmptyCells(Row row) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(row.cellIterator(), Spliterator.ORDERED), false)
        .map(ExcelUtils::getCellContentAsString)
        .filter(cellValue -> !cellValue.isBlank())
        .count();
  }

  /**
   * Get the content of a cell as string representation.
   *
   * @param cell The cell to get the content from
   * @return The cells content if present, else the blank string ""
   */
  public static String getCellContentAsString(Cell cell) {
    return switch (cell.getCellType()) {
      case NUMERIC -> String.valueOf(cell.getNumericCellValue());
      case STRING -> cell.getStringCellValue();
      case FORMULA -> cell.getCellFormula();
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case ERROR -> String.valueOf(cell.getErrorCellValue());
      default -> "";
    };
  }

  public static Optional<Locale> getWorkbookLanguage(XSSFWorkbook workbook) {
    var workbookLanguage = workbook.getProperties().getCoreProperties().getUnderlyingProperties().getLanguageProperty();
    return workbookLanguage.map(Locale::forLanguageTag);
  }
}
