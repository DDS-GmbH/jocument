package com.docutools.jocument.impl.excel.interfaces;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;

/**
 * This interface defines the methods used to interact with an excel writer.
 * This layer of indirection allows to use different methods of excel writers (XSSF, SXSSF,...), or even appropriate
 * writers for other file formats (csv,...).
 * As can be seen, for each creation of a new significant object, the desired object has to be passed.
 * Whether this objects should be added directly or only serve as references for copying is left to the implementation.
 */
public interface ExcelWriter {
    void addCell(Cell cell);

    void newSheet(Sheet sheet);

    void newRow(Row row);

    /**
     * Complete the creation of the report, writing the workbook to the earlier specified path.
     * @throws IOException If writing out of the workbook fails.
     */
    void complete() throws IOException;
}
