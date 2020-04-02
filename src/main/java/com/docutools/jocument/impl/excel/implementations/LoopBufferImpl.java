package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.impl.excel.interfaces.LoopBuffer;
import com.docutools.jocument.impl.excel.util.RowCellsTuple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.LinkedList;
import java.util.List;

public class LoopBufferImpl implements LoopBuffer {

    private LinkedList<RowCellsTuple> buffer;

    @Override
    public void startLoop() {
        buffer = new LinkedList<>();
    }

    @Override
    public List<RowCellsTuple> endLoop() {
        return buffer;
    }

    @Override
    public void addCell(Cell cell) {
        buffer.getLast().addCell(cell);
    }

    @Override
    public void newSheet(Sheet sheet) {
        throw new UnsupportedOperationException("Loops can not span multiple sheets");
    }

    @Override
    public void newRow(Row row) {
        buffer.addLast(new RowCellsTuple(row));
    }

    @Override
    public void complete() {
        throw new UnsupportedOperationException("Sheet finished without closing loop");
    }
}
