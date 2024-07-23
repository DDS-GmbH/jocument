package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * This is a streamed implementation of the {@link ExcelWriter} interface. The streaming is done so memory can be saved. For now, the amount of rows
 * kept in memory is set to the default, 100. SXSSFWriter works by keeping a reference to the current sheet and row being edited, and copying/cloning
 * required values on the creation of new objects. This is why to the `new...`/`add...` methods the original references of the template should be
 * passed. If one would like to use objects created somewhere else directly, a new implementation considering this would have to be created.
 *
 * @author Anton Oellerer
 * @since 2020-04-02
 */
public class XSSFWriter implements ExcelWriter {
  private static final Logger logger = LogManager.getLogger();

  private final Workbook workbook;
  private final CreationHelper creationHelper;
  /**
   * Maps the {@link CellStyle} objects of the old workbook to the new ones.
   */
  private final Map<Integer, CellStyle> cellStyleMap = new HashMap<>();
  private Sheet currentSheet;
  private Row currentRow;
  private int rowOffset = 0;
  private int leftMostColumn = -1;
  private int rightMostColumn = -1;
  private final Set<Integer> ignoredRows = new HashSet<>();

  /**
   * Creates a new SXSSFWriter.
   *
   */
  public XSSFWriter(Workbook workbook) {
    this.creationHelper = workbook.getCreationHelper();
    this.workbook = workbook;
  }

  @Override
  public void newSheet(Sheet sheet) {
    currentSheet = sheet;
  }

  @Override
  public void newRow(Row row) {
    logger.debug("Creating new row {}", row.getRowNum() + rowOffset - 1 - ignoredRows.size() * 2); //row num is 0 based
    currentRow = currentSheet.createRow(row.getRowNum() + rowOffset - 1 - ignoredRows.size() * 2);
    currentRow.setHeight(row.getHeight());
    if (row.isFormatted()) {
      currentRow.setRowStyle(cellStyleMap.computeIfAbsent((int) row.getRowStyle().getIndex(), i -> copyCellStyle(row.getRowStyle())));
    }
    currentRow.setZeroHeight(row.getZeroHeight());
    updateColumnStyles(row);
  }

  /**
   * Set the style of the columns accessed in this row.
   *
   * <p>Since apache POI does not have a global `leftMostColumn` and `rightMostColumn`, it is not possible to copy the default styles of each column
   * at the start of the report generation. Due to this, on each new row we check whether it accesses columns for which the default column style has
   * not been set yet, and copies the style to the new document.
   *
   * @param row The row to check for border column accesses
   */
  private void updateColumnStyles(Row row) {
    if (rightMostColumn == -1 || leftMostColumn == -1) {
      setup(row);
    }
    if (row.getLastCellNum() > rightMostColumn) {
      for (; rightMostColumn < row.getLastCellNum(); rightMostColumn++) {
        copyColumnStyle(rightMostColumn);
      }
      logger.debug("Rightmost column: {}", rightMostColumn);
    }
    if (row.getFirstCellNum() < leftMostColumn) {
      for (; leftMostColumn > row.getLastCellNum(); leftMostColumn--) {
        copyColumnStyle(leftMostColumn);
      }
      logger.debug("Leftmost column: {}", leftMostColumn);
    }
  }

  private void setup(Row row) {
    leftMostColumn = row.getFirstCellNum();
    rightMostColumn = row.getLastCellNum();
    logger.debug("Rightmost column: {}", rightMostColumn);
    logger.debug("Leftmost column: {}", leftMostColumn);
    for (int i = leftMostColumn; i < rightMostColumn; i++) {
      copyColumnStyle(i);
    }
  }

  private void copyColumnStyle(int rightMostColumn) {
    CellStyle columnStyle = currentSheet.getColumnStyle(rightMostColumn);
    if (columnStyle != null) {
      currentSheet.setDefaultColumnStyle(rightMostColumn,
          cellStyleMap.computeIfAbsent((int) columnStyle.getIndex(), i -> copyCellStyle(columnStyle)));
    }
  }

  @Override
  public void addCell(Cell cell) {
    logger.trace("Creating new cell {} {}", cell.getColumnIndex(), cell.getRow().getRowNum());
    var newCell = createNewCell(cell, 0);
    switch (cell.getCellType()) {
      case NUMERIC -> newCell.setCellValue(cell.getNumericCellValue());
      case STRING -> newCell.setCellValue(cell.getStringCellValue());
      case FORMULA -> newCell.setCellFormula(cell.getCellFormula());
      case BLANK -> newCell.setBlank();
      case BOOLEAN -> newCell.setCellValue(cell.getBooleanCellValue());
      case ERROR -> newCell.setCellErrorValue(cell.getErrorCellValue());
      default -> {
        // do nothing
      }
    }
    if (cell.getHyperlink() != null) {
      Hyperlink hyperlink = cell.getHyperlink();
      Hyperlink newHyperlink = creationHelper.createHyperlink(hyperlink.getType());
      newHyperlink.setAddress(hyperlink.getAddress());
      newHyperlink.setLabel(hyperlink.getLabel());
      newCell.setHyperlink(newHyperlink);
    }
  }

  @Override
  public void addCell(Cell templateCell, String newCellText) {
    addCell(templateCell, newCellText, 0);
  }

  @Override
  public void addCell(Cell templateCell, double newCellValue) {
    addCell(templateCell, newCellValue, 0);
  }

  @Override
  public void addCell(Cell templateCell, String newCellText, int columnOffset) {
    logger.trace("Creating new cell {} {} with text {}",
        templateCell.getColumnIndex() + columnOffset, templateCell.getRow().getRowNum(), newCellText);
    var newCell = createNewCell(templateCell, columnOffset);
    if (templateCell.getCellType() == CellType.FORMULA) {
      newCell.setCellFormula(newCellText);
    } else {
      newCell.setCellValue(newCellText);
    }
  }

  @Override
  public void addCell(Cell templateCell, double newCellValue, int columnOffset) {
    logger.trace("Creating new cell {} {} with double value {} and offset {}",
        templateCell.getColumnIndex(), templateCell.getRow().getRowNum(), newCellValue, columnOffset);
    var newCell = createNewCell(templateCell, columnOffset);
    newCell.setCellValue(newCellValue);
  }

  private Cell createNewCell(Cell templateCell, int columnOffset) {
    var newCell = currentRow.createCell(templateCell.getColumnIndex() + columnOffset, templateCell.getCellType());
    newCell.setCellComment(templateCell.getCellComment());
    newCell.setCellStyle(cellStyleMap.computeIfAbsent((int) templateCell.getCellStyle().getIndex(), i -> copyCellStyle(templateCell.getCellStyle())));
    newCell.setHyperlink(templateCell.getHyperlink());
    currentSheet.setColumnWidth(templateCell.getColumnIndex(), templateCell.getSheet().getColumnWidth(templateCell.getColumnIndex()));
    return newCell;
  }

  @Override
  public void complete() throws IOException {
  }

  @Override
  public void addRowOffset(int size) {
    rowOffset += size;
  }

  @Override
  public void recalculateFormulas() {
  }

  @Override
  public void setRow(Row row) {
    this.currentRow = row;
  }

  @Override
  public void deleteRows(int loopStart, int noRows) {
    for (int i = loopStart; i < loopStart + noRows; i++) {
      Row row = currentSheet.getRow(i);
      if (row != null) {
        currentSheet.removeRow(row);
      }
    }
    currentSheet.shiftRows(loopStart + noRows, currentSheet.getLastRowNum(), -noRows);
  }

  @Override
  public void shiftRows(int startingRow, int toShift) {
    //rows are 1 indexed, row nums 0
    if (startingRow + rowOffset - 1 - ignoredRows.size() * 2  <= currentSheet.getLastRowNum()) {
      currentSheet.shiftRows(startingRow + rowOffset - 1 - ignoredRows.size() * 2, currentSheet.getLastRowNum(), toShift);
    }
  }

  @Override
  public void resetRowOffset() {
    this.rowOffset = 0;
    ignoredRows.clear();
  }

  @Override
  public void addIgnoreRow(int row) {
    ignoredRows.add(row);
  }

  private CellStyle copyCellStyle(CellStyle cellStyle) {
    var newStyle = workbook.createCellStyle();
    newStyle.cloneStyleFrom(cellStyle);
    return newStyle;
  }
}
