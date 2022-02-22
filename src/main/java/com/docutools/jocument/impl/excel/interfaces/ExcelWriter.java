package com.docutools.jocument.impl.excel.interfaces;

import java.io.IOException;
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
   * Complete the creation of the report, writing the workbook to the earlier specified path.
   *
   * @throws IOException If writing out of the workbook fails.
   */
  void complete() throws IOException;

  /**
   * Add a row offset to consider when creating new rows.
   * This has to be done for example when unrolling loops, since the original rows still have the old indices,
   * possibly pointing to already written rows
   *
   * @param size The number of rows to add to the row index of rows to clone when creating new rows
   */
  void addRowOffset(int size);

  /**
   * Create a new cell from the templateCell with the specified cell text.
   * This has to be done e.g. for template cells in loops which have to be used multiple times.
   *
   * @param templateCell The template cell to create the new cell from
   * @param newCellText  The text to insert into the cell
   */
  void addCell(Cell templateCell, String newCellText);

  void addCell(Cell cell);

  void recalculateFormulas();
}
