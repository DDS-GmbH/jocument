package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class SXSSFWriter implements ExcelWriter {
    private final Path path;
    private final SXSSFWorkbook workbook;
    private Sheet currentSheet;
    private Row currentRow;

    public SXSSFWriter(Path path) {
        workbook = new SXSSFWorkbook();
        this.path = path;
    }

    @Override
    public void newSheet(Sheet sheet) {
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
        currentSheet.setForceFormulaRecalculation(true); //done since we cannot access every cell at the same time for recalculation
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
    }

    @Override
    public void newRow(Row row) {
        currentRow = currentSheet.createRow(row.getRowNum());
        row.setHeight(row.getHeight());
        row.setRowStyle(row.getRowStyle());
        row.setZeroHeight(row.getZeroHeight());
    }

    @Override
    public void addCell(Cell cell) {
        var newCell = currentRow.createCell(cell.getColumnIndex(), cell.getCellType());
        newCell.setCellComment(cell.getCellComment());
        newCell.setCellStyle(cell.getCellStyle());
        newCell.setHyperlink(cell.getHyperlink());
        switch (cell.getCellType()) {
            case _NONE -> {
            }
            case NUMERIC -> newCell.setCellValue(cell.getNumericCellValue());
            case STRING -> newCell.setCellValue(cell.getStringCellValue());
            case FORMULA -> newCell.setCellFormula(cell.getCellFormula());
            case BLANK -> newCell.setBlank();
            case BOOLEAN -> newCell.setCellValue(cell.getBooleanCellValue());
            case ERROR -> newCell.setCellErrorValue(cell.getErrorCellValue());
        }
    }

    @Override
    public void complete() throws IOException {
        var outputStream = new BufferedOutputStream(Files.newOutputStream(path));
        workbook.write(outputStream);
        outputStream.close();
        workbook.dispose();
    }
}
