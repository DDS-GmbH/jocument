package com.docutools.jocument.impl.excel.interfaces;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * This interface defines the methods used to interact with an excel writer.
 * This layer of indirection allows to use different methods of excel writers (XSSF, SXSSF,...), or even appropriate
 * writers for other file formats (csv,...).
 * As can be seen, for each creation of a new significant object, the desired object has to be passed.
 * Whether this objects should be added directly or only serve as references for copying is left to the implementation.
 */
public interface ExcelWriter {
  void newSheet(Sheet sheet);

  void newRow(Row row);

  /**
   * Create a new cell from the templateCell with the specified cell text.
   *
   * @param templateCell The template cell to create the new cell from
   * @param newCellText  The text to insert into the cell
   */
  void addCell(Cell templateCell, String newCellText);

  /**
   * Create a new cell from the templateCell with the specified cell value.
   *
   * @param templateCell The template cell to create the new cell from
   * @param newCellValue The numeric value to insert into the cell
   */
  void addCell(Cell templateCell, double newCellValue);

  /**
   * Create a new cell from the templateCell with the specified cell text and the specified offset. This has to be done e.g. for template cells in
   * loops which have to be used multiple times.
   *
   * @param templateCell The template cell to create the new cell from
   * @param newCellText  The text to insert into the cell
   * @param columnOffset The column offset to apply to the new cell
   */
  void addCell(Cell templateCell, String newCellText, int columnOffset);

  /**
   * Create a new cell from the templateCell with the specified cell text and the specified offset. This has to be done e.g. for template cells in
   * loops which have to be used multiple times.
   *
   * @param templateCell The template cell to create the new cell from
   * @param newCellValue The numeric value to insert into the cell
   * @param columnOffset The column offset to apply to the new cell
   */
  void addCell(Cell templateCell, double newCellValue, int columnOffset);

  void addCell(Cell cell);

  void setRow(Row row);

  void deleteRows(int loopStart, int noRows);

  void shiftRows(int startingRow, int loopBodySize);

  void updateRowsWritten(int rows);

  void addRowToIgnore(int row);

  void setSectionOffset(int rows);

  void finishLoopProcessing(int rowNum, int loopSize);
}
