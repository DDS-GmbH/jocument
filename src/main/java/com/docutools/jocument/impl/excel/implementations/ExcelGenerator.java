package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.ParsingUtils;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.util.ExcelUtils;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * This class is responsible for creating excel report rows from template rows.
 * It is used recursively when resolving nested loops.
 * Because of this, the generator is agnostic of enclosing structures like sheets and workbooks.
 *
 * @author Anton Oellerer
 * @since 2020-04
 * @version 1.1.0
 */
public class ExcelGenerator {
    private final ExcelWriter excelWriter;
    private final PlaceholderResolver resolver;
    private final Iterator<Row> rowIterator;

    private ExcelGenerator(Iterator<Row> rowIterator, ExcelWriter excelWriter, PlaceholderResolver resolver) {
        this.rowIterator = rowIterator;
        this.excelWriter = excelWriter;
        this.resolver = resolver;
    }

    /**
     * This function starts the generating process for the supplied row iterator.
     * @param resolver The resolver to use for looking up placeholders
     * @param rowIterator An iterator over the template row which should be processed
     * @param excelWriter The writer to write the report out to.
     */
    static void apply(PlaceholderResolver resolver, Iterator<Row> rowIterator, ExcelWriter excelWriter) {
        new ExcelGenerator(rowIterator, excelWriter, resolver).generate();
    }

    private void generate() {
        for (Iterator<Row> iterator = rowIterator; iterator.hasNext(); ) {
            Row row = iterator.next();

            if (isLoopStart(row)) {
                var loopBody = unrollLoop(row, iterator);
                var loopBodySize = getLoopBodySize(loopBody);
                var placeholderData = getPlaceholderData(row);
                placeholderData.stream()
                        .forEach(placeholderResolver -> {
                            excelWriter.addRowOffset(-1); //So we also fill the cell of the loop start placeholder
                            ExcelGenerator.apply(placeholderResolver, loopBody.iterator(), excelWriter);
                            excelWriter.addRowOffset(1); //To avoid subtracting the placeholder size multiple times
                            excelWriter.addRowOffset(loopBodySize);
                        });
            } else if (!ExcelUtils.isLoopEnd(row)) {
                excelWriter.newRow(row);
                for (Cell cell : row) {
                    if (ExcelUtils.isSimpleCell(cell)) {
                        excelWriter.addCell(cell);
                    } else {
                        var newCellText = resolver.resolve(ExcelUtils.getPlaceholder(cell)).orElseThrow();
                        excelWriter.addCell(cell, newCellText.toString());
                    }
                }
            } else {
                excelWriter.addRowOffset(-1);
            }
        }
    }

    private int getLoopBodySize(List<Row> loopBody) {
        var size = 0;
        Optional<String> inLoop = Optional.empty();
        for (Row row : Lists.reverse(loopBody)) {
            if (inLoop.isEmpty() && !ExcelUtils.isLoopEnd(row)) {
                size += 1;
            } else if (inLoop.isEmpty() && ExcelUtils.isLoopEnd(row)) {
                inLoop = Optional.of(ExcelUtils.getPlaceholderFromLoopEnd(row));
            } else if (inLoop.isPresent() && ExcelUtils.isMatchingLoopStart(row, inLoop.get())) {
                inLoop = Optional.empty();
            }
        }
        return size;
    }


    private List<Row> unrollLoop(Row row, Iterator<Row> iterator) {
        var placeholder = ExcelUtils.getPlaceholder(row);
        LinkedList<Row> rowBuffer = new LinkedList<>();
        var rowInFocus = iterator.next();
        while (!ExcelUtils.isMatchingLoopEnd(rowInFocus, placeholder)) {
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
