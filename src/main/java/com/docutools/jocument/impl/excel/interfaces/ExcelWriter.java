package com.docutools.jocument.impl.excel.interfaces;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;

public interface ExcelWriter {
    void addCell(Cell cell);

    void newSheet(Sheet sheet);

    void newRow(Row row);

    void complete() throws IOException;
}
