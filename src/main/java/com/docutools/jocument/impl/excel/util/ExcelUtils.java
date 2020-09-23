package com.docutools.jocument.impl.excel.util;

import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.ParsingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;
import java.util.stream.StreamSupport;

public class ExcelUtils {
    private static final Logger logger = LogManager.getLogger();

    public static String getPlaceholder(Cell cell) {
        return ParsingUtils.stripBrackets(cell.getStringCellValue());
    }

    public static boolean isSimpleCell(Cell cell) {
        return cell.getCellType() != CellType.STRING
                || cell.getCellType() == CellType.STRING && !DocumentImpl.TAG_PATTERN.asPredicate().test(cell.getStringCellValue());
    }

    public static Cell replaceCellContent(Cell cell, String replacement) {
        logger.debug("Replacing content of cell {} with string {}", cell.getStringCellValue(), replacement);
        cell.setCellValue(replacement);
        return cell;
    }

    public static boolean isLoopEnd(Row row) {
        if (row.getPhysicalNumberOfCells() == 1) {
            var cell = row.getCell(row.getFirstCellNum());
            if (cell.getCellType() == CellType.STRING) {
                return DocumentImpl.LOOP_END_PATTERN.asPredicate().test(cell.getStringCellValue());
            }
        }
        return false;
    }

    public static String getPlaceholder(Row row) {
        return ParsingUtils.stripBrackets(row.getCell(row.getFirstCellNum()).getStringCellValue());
    }

    public static String getPlaceholderFromLoopEnd(Row row) {
        return ParsingUtils.stripBrackets(row.getCell(row.getFirstCellNum()).getStringCellValue()).substring(1);
    }

    public static boolean isMatchingLoopEnd(Row row, String placeholder) {
        var endPlaceholder = ParsingUtils.getMatchingLoopEnd(placeholder);
        if (getNumberOfNonEmptyCells(row) == 1) {
            var cell = row.getCell(row.getFirstCellNum());
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().equals(endPlaceholder);
            }
        }
        return false;
    }

    public static boolean isMatchingLoopStart(Row row, String placeholder) {
        if (row.getPhysicalNumberOfCells() == 1) {
            var cell = row.getCell(row.getFirstCellNum());
            if (cell.getCellType() == CellType.STRING) {
                return ParsingUtils.stripBrackets(cell.getStringCellValue()).equals(placeholder);
            }
        }
        return false;
    }

    private static long getNumberOfNonEmptyCells(Row row) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(row.cellIterator(), Spliterator.ORDERED), false)
                .map(Cell::getStringCellValue)
                .filter(cellValue -> !cellValue.isBlank())
                .count();
    }

    public static boolean isSimpleRow(Row row) {
        var isSimpleRow = true;
        for (Iterator<Cell> it = row.cellIterator(); it.hasNext(); ) {
            Cell cell = it.next();
            isSimpleRow &= isSimpleCell(cell);
        }
        return isSimpleRow;
    }

    public static Optional<Locale> getWorkbookLanguage(XSSFWorkbook workbook) {
        var workbookLanguage = workbook.getProperties().getCoreProperties().getUnderlyingProperties().getLanguageProperty();
        return workbookLanguage.map(Locale::forLanguageTag);
    }
}
