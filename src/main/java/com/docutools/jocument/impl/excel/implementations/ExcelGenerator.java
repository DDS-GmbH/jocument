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
    private final int nestedLoopDepth;
    private int alreadyProcessedLoopsSize = 0;

    private ExcelGenerator(Iterator<Row> rowIterator, ExcelWriter excelWriter, PlaceholderResolver resolver, int nestedLoopDepth) {
        this.rowIterator = rowIterator;
        this.excelWriter = excelWriter;
        this.resolver = resolver;
        this.nestedLoopDepth = nestedLoopDepth;
    }

    /**
     * This function starts the generating process for the supplied row iterator.
     *
     * @param resolver    The resolver to use for looking up placeholders
     * @param rowIterator An iterator over the template row which should be processed
     * @param excelWriter The writer to write the report out to.
     */
    static void apply(PlaceholderResolver resolver, Iterator<Row> rowIterator, ExcelWriter excelWriter) {
        apply(resolver, rowIterator, excelWriter, 0);
    }

    private static void apply(PlaceholderResolver resolver, Iterator<Row> rowIterator, ExcelWriter excelWriter, int nestedLoopDepth) {
        new ExcelGenerator(rowIterator, excelWriter, resolver, nestedLoopDepth).generate();
    }

    private void generate() {
        for (Iterator<Row> iterator = rowIterator; iterator.hasNext(); ) {
            Row row = iterator.next();

            if (isLoopStart(row)) {
                handleLoop(row, iterator);
            } else {
                excelWriter.newRow(row);
                for (Cell cell : row) {
                    if (ExcelUtils.isSimpleCell(cell)) {
                        excelWriter.addCell(cell);
                    } else {
                        var newCellText = resolver.resolve(ExcelUtils.getPlaceholder(cell)).orElseThrow();
                        excelWriter.addCell(cell, newCellText.toString());
                    }
                }
            }
        }
        if (nestedLoopDepth != 0) { //here for clarity, could be removed since generation finishes if nestedLoopDepth == 0
            excelWriter.addRowOffset(alreadyProcessedLoopsSize); //we are in nested loop, readd the offset to prevent subtracting it multiple times
        }
    }

    private void handleLoop(Row row, Iterator<Row> iterator) {
        var loopBody = getLoopBody(row, iterator);
        var loopBodySize = getLoopBodySize(loopBody);
        var finalLoopBody = loopBody.subList(1, loopBody.size() - 1);
        var placeholderData = getPlaceholderData(row);
        placeholderData.stream()
                .forEach(placeholderResolver -> {
                    excelWriter.addRowOffset(-1); //So we also fill the cell of the loop start placeholder
                    ExcelGenerator.apply(placeholderResolver, finalLoopBody.iterator(), excelWriter, nestedLoopDepth + 1);
                    excelWriter.addRowOffset(1); //To avoid subtracting the placeholder size multiple times
                    excelWriter.addRowOffset(loopBodySize);
                });
        var loopPlaceholderSize = getLoopSize(loopBody);
        excelWriter.addRowOffset(-1 * loopPlaceholderSize);
        alreadyProcessedLoopsSize += loopPlaceholderSize;
    }

    private List<Row> getLoopBody(Row row, Iterator<Row> iterator) {
        var placeholder = ExcelUtils.getPlaceholder(row);
        LinkedList<Row> rowBuffer = new LinkedList<>();
        rowBuffer.add(row);
        var rowInFocus = iterator.next();
        while (!ExcelUtils.isMatchingLoopEnd(rowInFocus, placeholder)) {
            rowBuffer.addLast(rowInFocus);
            rowInFocus = iterator.next();
        }
        rowBuffer.addLast(rowInFocus);
        return rowBuffer;
    }

    private int getLoopBodySize(List<Row> loopBody) {
        var size = loopBody.get(loopBody.size() - 1).getRowNum() - loopBody.get(0).getRowNum() + 1;  //inclusive
        Optional<String> inLoop = Optional.empty();
        Optional<Integer> loopEnd = Optional.empty();
        for (Row row : Lists.reverse(loopBody.subList(1, loopBody.size() - 1))) {
            if (inLoop.isEmpty() && ExcelUtils.isLoopEnd(row)) {
                inLoop = Optional.of(ExcelUtils.getPlaceholderFromLoopEnd(row));
                loopEnd = Optional.of(row.getRowNum());
            } else if (inLoop.isPresent() && ExcelUtils.isMatchingLoopStart(row, inLoop.get())) {
                inLoop = Optional.empty();
                size -= loopEnd.orElseThrow() - row.getRowNum() + 1; //inclusive
                loopEnd = Optional.empty();
            }
        }
        return size - 2; //loop start/end placeholders
    }

    private int getLoopSize(List<Row> loopBody) {
        return loopBody.get(loopBody.size() - 1).getRowNum() - loopBody.get(0).getRowNum() + 1;  //inclusive
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
