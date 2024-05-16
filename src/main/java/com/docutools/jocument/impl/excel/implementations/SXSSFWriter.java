package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFFormulaEvaluator;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * This is a streamed implementation of the {@link com.docutools.jocument.impl.excel.interfaces.ExcelWriter} interface. The streaming is done so
 * memory can be saved. For now, the amount of rows kept in memory is set to the default, 100. SXSSFWriter works by keeping a reference to the current
 * sheet and row being edited, and copying/cloning required values on the creation of new objects. This is why to the `new...`/`add...` methods the
 * original references of the template should be passed. If one would like to use objects created somewhere else directly, a new implementation
 * considering this would have to be created.
 *
 * @author Anton Oellerer
 * @since 2020-04-02
 */
public class SXSSFWriter implements ExcelWriter {
  private static final Logger logger = LogManager.getLogger();

  private final Path path;
  private final SXSSFWorkbook workbook;
  private final CreationHelper creationHelper;
  /**
   * Maps the {@link CellStyle} objects of the old workbook to the new ones.
   */
  private final Map<Integer, CellStyle> cellStyleMap = new HashMap<>();
  private Sheet currentSheet;
  private Sheet templateSheet;
  private Row currentRow;
  private int rowOffset = 0;
  private int leftMostColumn = -1;
  private int rightMostColumn = -1;

  /**
   * Creates a new SXSSFWriter.
   *
   * @param path The path to save the finished report to.
   */
  public SXSSFWriter(Path path) {
    workbook = new SXSSFWorkbook();
    this.creationHelper = workbook.getCreationHelper();
    this.path = path;
  }

  private static void transferPicture(XSSFShape shape, SXSSFSheet newSheet) {
    XSSFPicture picture = (XSSFPicture) shape;

    XSSFPictureData xssfPictureData = picture.getPictureData();
    XSSFClientAnchor anchor = (XSSFClientAnchor) shape.getAnchor();

    int col1 = anchor.getCol1();
    int col2 = anchor.getCol2();
    int row1 = anchor.getRow1();
    int row2 = anchor.getRow2();

    int x1 = anchor.getDx1();
    int x2 = anchor.getDx2();
    int y1 = anchor.getDy1();
    int y2 = anchor.getDy2();

    var newWb = newSheet.getWorkbook();
    var newHelper = newWb.getCreationHelper();
    var newAnchor = newHelper.createClientAnchor();

    // Row / Column placement.
    newAnchor.setCol1(col1);
    newAnchor.setCol2(col2);
    newAnchor.setRow1(row1);
    newAnchor.setRow2(row2);

    // Fine touch adjustment along the XY coordinate.
    newAnchor.setDx1(x1);
    newAnchor.setDx2(x2);
    newAnchor.setDy1(y1);
    newAnchor.setDy2(y2);

    int newPictureIndex = newWb.addPicture(xssfPictureData.getData(), xssfPictureData.getPictureType());

    var newDrawing = newSheet.createDrawingPatriarch();
    newDrawing.createPicture(newAnchor, newPictureIndex);
  }

  @Override
  public void newSheet(Sheet sheet) {
    logger.info("Creating new sheet of {}", sheet.getSheetName());
    templateSheet = sheet;
    currentSheet = workbook.createSheet(sheet.getSheetName());
    currentSheet.setActiveCell(sheet.getActiveCell());
    currentSheet.setAutobreaks(sheet.getAutobreaks());
    Arrays.stream(sheet.getColumnBreaks()).forEach(column -> currentSheet.setColumnBreak(column));
    currentSheet.setDefaultColumnWidth(sheet.getDefaultColumnWidth());
    currentSheet.setDefaultRowHeight(sheet.getDefaultRowHeight());
    currentSheet.setDisplayFormulas(sheet.isDisplayFormulas());
    currentSheet.setDisplayGridlines(sheet.isDisplayGridlines());
    currentSheet.setDisplayGuts(sheet.getDisplayGuts());
    currentSheet.setDisplayRowColHeadings(sheet.isDisplayRowColHeadings());
    currentSheet.setDisplayZeros(sheet.isDisplayZeros());
    currentSheet.setFitToPage(sheet.getFitToPage());
    currentSheet.setHorizontallyCenter(sheet.getHorizontallyCenter());
    currentSheet.setPrintGridlines(sheet.isPrintGridlines());
    currentSheet.setPrintRowAndColumnHeadings(sheet.isPrintRowAndColumnHeadings());
    currentSheet.setRepeatingColumns(sheet.getRepeatingColumns());
    currentSheet.setRepeatingRows(sheet.getRepeatingRows());
    currentSheet.setRightToLeft(sheet.isRightToLeft());
    Arrays.stream(sheet.getRowBreaks()).forEach(row -> currentSheet.setRowBreak(row));
    currentSheet.setRowSumsBelow(sheet.getRowSumsBelow());
    currentSheet.setRowSumsRight(sheet.getRowSumsRight());
    currentSheet.setSelected(sheet.isSelected());
    currentSheet.setVerticallyCenter(sheet.getVerticallyCenter());

    // copy auto filters to new sheet
    if (sheet instanceof XSSFSheet xssfSheet) {
      var autoFilter = xssfSheet.getCTWorksheet().getAutoFilter();
      if (autoFilter != null) {
        var ref = autoFilter.getRef();
        var range = CellRangeAddress.valueOf(ref);
        currentSheet.setAutoFilter(range);
      }
    }

    var drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
    for (var shape : drawing.getShapes()) {
      if (shape instanceof XSSFPicture) {
        transferPicture(shape, (SXSSFSheet) currentSheet);
      }
    }
  }

  @Override
  public void newRow(Row row) {
    logger.debug("Creating new row {}", row.getRowNum());
    currentRow = currentSheet.createRow(row.getRowNum() + rowOffset);
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
    CellStyle columnStyle = templateSheet.getColumnStyle(rightMostColumn);
    if (columnStyle != null) {
      currentSheet.setDefaultColumnStyle(rightMostColumn,
          cellStyleMap.computeIfAbsent((int) columnStyle.getIndex(), i -> copyCellStyle(columnStyle)));
    }
  }

  @Override
  public void addCell(Cell cell) {
    logger.trace("Creating new cell {} {}", cell.getColumnIndex(), cell.getRow().getRowNum());
    var newCell = currentRow.createCell(cell.getColumnIndex(), cell.getCellType());
    newCell.setCellComment(cell.getCellComment());
    newCell.setCellStyle(cellStyleMap.computeIfAbsent((int) cell.getCellStyle().getIndex(), i -> copyCellStyle(cell.getCellStyle())));
    newCell.setHyperlink(cell.getHyperlink());
    currentSheet.setColumnWidth(cell.getColumnIndex(), cell.getSheet().getColumnWidth(cell.getColumnIndex()));
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
    //todo merge with addCell(Cell, String, int)
    logger.trace("Creating new cell {} {} with text {}",
        templateCell.getColumnIndex(), templateCell.getRow().getRowNum(), newCellValue);
    var newCell = currentRow.createCell(templateCell.getColumnIndex(), templateCell.getCellType());
    newCell.setCellComment(templateCell.getCellComment());
    newCell.setCellStyle(cellStyleMap.computeIfAbsent((int) templateCell.getCellStyle().getIndex(), i -> copyCellStyle(templateCell.getCellStyle())));
    newCell.setHyperlink(templateCell.getHyperlink());
    currentSheet.setColumnWidth(templateCell.getColumnIndex(), templateCell.getSheet().getColumnWidth(templateCell.getColumnIndex()));
    newCell.setCellValue(newCellValue);
  }

  @Override
  public void addCell(Cell templateCell, String newCellText, int columnOffset) {
    logger.trace("Creating new cell {} {} with text {}",
        templateCell.getColumnIndex(), templateCell.getRow().getRowNum(), newCellText);
    var newCell = currentRow.createCell(templateCell.getColumnIndex() + columnOffset, templateCell.getCellType());
    newCell.setCellComment(templateCell.getCellComment());
    newCell.setCellStyle(cellStyleMap.computeIfAbsent((int) templateCell.getCellStyle().getIndex(), i -> copyCellStyle(templateCell.getCellStyle())));
    newCell.setHyperlink(templateCell.getHyperlink());
    currentSheet.setColumnWidth(templateCell.getColumnIndex(), templateCell.getSheet().getColumnWidth(templateCell.getColumnIndex()));
    if (templateCell.getCellType() == CellType.FORMULA) {
      newCell.setCellFormula(newCellText);
    } else {
      newCell.setCellValue(newCellText);
    }
  }

  @Override
  public void complete() throws IOException {
    var outputStream = new BufferedOutputStream(Files.newOutputStream(path));
    workbook.write(outputStream);
    outputStream.close();
    workbook.dispose();
  }

  @Override
  public void addRowOffset(int size) {
    rowOffset += size;
  }

  @Override
  public void recalculateFormulas() {
    SXSSFFormulaEvaluator.evaluateAllFormulaCells(workbook, true);
  }

  private CellStyle copyCellStyle(CellStyle cellStyle) {
    var newStyle = workbook.createCellStyle();
    newStyle.cloneStyleFrom(cellStyle);
    return newStyle;
  }
}
