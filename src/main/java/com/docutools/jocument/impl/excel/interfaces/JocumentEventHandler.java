package com.docutools.jocument.impl.excel.interfaces;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public interface JocumentEventHandler {
    void simpleCell(Cell cell);

    void startLoop();

    void endLoop();

    void newSheet(Sheet sheet);

    void newRow(Row row);

    void placeholderCell(String placeholder, Cell cell);
}
