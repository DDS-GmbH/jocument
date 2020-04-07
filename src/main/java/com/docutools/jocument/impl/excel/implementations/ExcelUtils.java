package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.ParsingUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class ExcelUtils {
    public static String getPlaceholder(Cell cell) {
        return ParsingUtils.stripBrackets(cell.getStringCellValue());
    }

    public static boolean isSimpleCell(Cell cell) {
        return cell.getCellType() != CellType.STRING
                || cell.getCellType() == CellType.STRING && !DocumentImpl.TAG_PATTERN.asPredicate().test(cell.getStringCellValue());
    }

    public static Cell replaceCellContent(Cell cell, String replacement) {
        cell.setCellValue(replacement);
        return cell;
    }

}
