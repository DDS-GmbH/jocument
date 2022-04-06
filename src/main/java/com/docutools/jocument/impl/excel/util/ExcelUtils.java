package com.docutools.jocument.impl.excel.util;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.ParsingUtils;
import com.docutools.jocument.impl.ScalarPlaceholderData;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
  private static final Logger logger = LogManager.getLogger();

  /**
   * Find and replace the placeholders found in value using given resolver.
   *
   * @param value     Text value with placeholders to be replaced.
   * @param resolver  {@link PlaceholderResolver} used to resolve the placeholders found in value.
   * @return          String with replaced placeholders.
   */
  public static String replacePlaceholders(String value, PlaceholderResolver resolver) {
    var matcher = ParsingUtils.matchPlaceholders(value);
    return matcher.replaceAll(matchResult -> resolver.resolve(matchResult.group(1))
        .orElse(new ScalarPlaceholderData("-"))
        .toString()
    );
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

  public static boolean isHyperlinkFormula(Cell cell) {
    return cell.getCellType() == CellType.FORMULA && cell.getCellFormula().startsWith("HYPERLINK");
  }

  /**
   * This method replaces the string content of a cell with the replacement string.
   *
   * @param cell        The cell where the new value should be inserted
   * @param replacement The string which should be inserted into the cell
   * @return The cell with the new value inserted
   */
  public static Cell replaceCellContent(Cell cell, String replacement) {
    logger.debug("Replacing content of cell {} with string {}", cell.getStringCellValue(), replacement);
    cell.setCellValue(replacement);
    return cell;
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
    var endPlaceholder = ParsingUtils.getMatchingLoopEnd(placeholder);
    if (getNumberOfNonEmptyCells(row) == 1) {
      var cell = row.getCell(row.getFirstCellNum());
      if (cell.getCellType() == CellType.STRING) {
        return cell.getStringCellValue().equals(endPlaceholder);
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

  /**
   * This method checks whether the passed row contains a placeholder.
   *
   * @param row The row to check for a placeholder
   * @return Whether the row contains a placeholder
   */
  public static boolean isSimpleRow(Row row) {
    var isSimpleRow = true;
    for (Iterator<Cell> it = row.cellIterator(); it.hasNext(); ) {
      Cell cell = it.next();
      isSimpleRow &= isSimpleCell(cell);
    }
    return isSimpleRow;
  }

  public static Optional<Locale> getWorkbookLanguage(XSSFWorkbook workbook) {
    var workbookLanguage = workbook.getProperties().getCoreProperties().getUnderlyingProperties().getLanguageProperty();
    return workbookLanguage.map(Locale::forLanguageTag);
  }
}
