package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.Template;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.interfaces.JocumentEventHandler;
import com.docutools.jocument.impl.excel.interfaces.LoopBuffer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.nio.file.Path;

public class ExcelGenerator implements JocumentEventHandler {
    private final Template template;
    private final LoopBuffer loopBuffer;
    private final ExcelWriter directWriter;
    private ExcelWriter excelWriter;

    public ExcelGenerator(Template template, Path outFile) {
        this.template = template;
        this.directWriter = new SXSSFWriter(outFile);
        this.excelWriter = directWriter;
        this.loopBuffer = new LoopBufferImpl();
    }

    @Override
    public void simpleCell(Cell cell) {
        excelWriter.addCell(cell);
    }

    @Override
    public void startLoop() {
        loopBuffer.startLoop();
        excelWriter = loopBuffer;
    }

    @Override
    public void endLoop() {
        excelWriter = directWriter;
        var rowCellsTuples = loopBuffer.endLoop();
        rowCellsTuples.forEach(
                rowCellsTuple -> {
                    this.directWriter.newRow(rowCellsTuple.getRow());
                    rowCellsTuple.getCells().forEach(this.directWriter::addCell);
                });
    }

    @Override
    public void newSheet(Sheet sheet) {
        excelWriter.newSheet(sheet);
    }

    @Override
    public void newRow(Row row) {
        excelWriter.newRow(row);
    }

    @Override
    public void placeholderCell(String placeholder, Cell cell) {

    }

    public void generate() {

    }
}
