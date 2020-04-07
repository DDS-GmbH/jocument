package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.ParsingUtils;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ExcelGenerator {
    private final ExcelWriter excelWriter;
    private final PlaceholderResolver resolver;
    private final Iterator<Row> rowIterator;

    private ExcelGenerator(Iterator<Row> rowIterator, ExcelWriter excelWriter, PlaceholderResolver resolver) {
        this.rowIterator = rowIterator;
        this.excelWriter = excelWriter;
        this.resolver = resolver;
    }

    static void apply(PlaceholderResolver resolver, Iterator<Row> rowIterator, ExcelWriter excelWriter) {
        new ExcelGenerator(rowIterator, excelWriter, resolver).generate();
    }

    private void generate() {
        for (Iterator<Row> iterator = rowIterator; iterator.hasNext(); ) {
            Row row = iterator.next();

            if (isLoopStart(row)) {
                var loopBody = unrollLoop(row, iterator);
                var placeholderData = getPlaceholderData(row);
                placeholderData.stream()
                        .forEach(placeholderResolver -> ExcelGenerator.apply(placeholderResolver, loopBody.iterator(), excelWriter));
            } else if (!ExcelUtils.isLoopEnd(row)){
                excelWriter.newRow(row);
                for (Cell cell : row) {
                    if (ExcelUtils.isSimpleCell(cell)) {
                        excelWriter.addCell(cell);
                    } else {
                        var substitutedCell = resolver.resolve(ExcelUtils.getPlaceholder(cell))
                                .map(placeholderData -> ExcelUtils.replaceCellContent(cell, placeholderData.toString()))
                                .orElseThrow();
                        excelWriter.addCell(substitutedCell);
                    }
                }
            }
        }
    }


    private List<Row> unrollLoop(Row row, Iterator<Row> iterator) {
        var placeholder = ParsingUtils.stripBrackets(row.getCell(row.getFirstCellNum()).getStringCellValue());
        LinkedList<Row> rowBuffer = new LinkedList<>();
        var rowInFocus = iterator.next();
        while (isMatchingLoopEnd(rowInFocus, placeholder)) {
            rowBuffer.addLast(rowInFocus);
            rowInFocus = iterator.next();
        }
        return rowBuffer;
    }

    private PlaceholderData getPlaceholderData(Row row) {
        var placeholder = ExcelUtils.getPlaceholder(row.getCell(row.getFirstCellNum()));
        return resolver
                .resolve(placeholder)
                .filter(p -> p.getType() == PlaceholderType.SET)
                .orElseThrow();
    }

    private boolean isMatchingLoopEnd(Row row, String placeholder) {
        var endPlaceholder = ParsingUtils.getMatchingLoopEnd(placeholder);
        if (row.getPhysicalNumberOfCells() == 1) {
            var cell = row.getCell(row.getFirstCellNum());
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().equals(endPlaceholder);
            }
        }
        return false;
    }

    private boolean isLoopStart(Row row) {
        if (row.getPhysicalNumberOfCells() == 1) {
            var cell = row.getCell(row.getFirstCellNum());
            if (cell.getCellType() == CellType.STRING) {
                return resolver.resolve(
                        ParsingUtils.stripBrackets(
                                cell.getStringCellValue()
                        )).map(PlaceholderData::getType)
                        .map(type -> type == PlaceholderType.SET)
                        .orElse(false);
            }
        }
        return false;
    }

}
