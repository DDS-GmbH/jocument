package com.docutools.jocument.impl.excel.interfaces;

import com.docutools.jocument.impl.excel.util.RowCellsTuple;

import java.util.List;

public interface LoopBuffer extends ExcelWriter {
    void startLoop();

    List<RowCellsTuple> endLoop();
}
