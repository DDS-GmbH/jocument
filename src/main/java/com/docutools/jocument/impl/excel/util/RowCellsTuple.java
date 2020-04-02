package com.docutools.jocument.impl.excel.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.LinkedList;
import java.util.List;

public class RowCellsTuple {
    private final Row row;
    private final List<Cell> cells;

    public RowCellsTuple(Row row) {
        this.row = row;
        cells = new LinkedList<>();
    }

    public Row getRow() {
        return row;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void addCell(Cell cell) {
        cells.add(cell);
    }
}
