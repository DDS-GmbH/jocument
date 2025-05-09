package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.ParsingUtils;
import com.docutools.jocument.impl.ScalarPlaceholderData;
import com.docutools.jocument.impl.excel.interfaces.ExcelPlaceholderData;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.util.ExcelUtils;
import com.docutools.jocument.impl.excel.util.ModificationInformation;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.LocaleUtil;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;


/**
 * This class is responsible for creating excel report rows from template rows.
 * It is used recursively when resolving nested loops.
 * Because of this, the generator is agnostic of enclosing structures like sheets and workbooks.
 *
 * @author Anton Oellerer
 * @since 2020-04-10
 */
public class ExcelGenerator {
  private static final Logger logger = LogManager.getLogger();

  private final ExcelWriter excelWriter;
  private final PlaceholderResolver resolver;
  private final List<Row> rows;
  private final int nestedLoopDepth;
  private final GenerationOptions options;

  private ExcelGenerator(List<Row> rows, ExcelWriter excelWriter, PlaceholderResolver resolver, int nestedLoopDepth,
                         GenerationOptions options) {
    this.rows = rows;
    this.excelWriter = excelWriter;
    this.resolver = resolver;
    this.nestedLoopDepth = nestedLoopDepth;
    this.options = options;
  }

  /**
   * This function starts the generating process for the supplied row iterator.
   *
   * @param resolver    The resolver to use for looking up placeholders
   * @param rows        The rows which should be processed
   * @param excelWriter The writer to write the report out to.
   * @param options     {@link GenerationOptions}
   */
  static void apply(PlaceholderResolver resolver, List<Row> rows, ExcelWriter excelWriter, GenerationOptions options) {
    apply(resolver, rows, excelWriter, 0, options);
  }

  private static void apply(PlaceholderResolver resolver, List<Row> rows, ExcelWriter excelWriter, int nestedLoopDepth,
                            GenerationOptions options) {
    new ExcelGenerator(rows, excelWriter, resolver, nestedLoopDepth, options).generate();
  }

  private void generate() {
    logger.debug("Starting generation by applying resolver {}", resolver);
    List<Row> toProcess = new LinkedList<>(rows);
    while (!toProcess.isEmpty()) {
      Row row = toProcess.get(0);
      toProcess = toProcess.subList(1, toProcess.size());
      try {
        if (isLoopStart(row)) {
          toProcess = handleLoop(row, toProcess);
        } else {
          handleRow(row);
        }
      } catch (XmlValueDisconnectedException e) {
        logger.warn(e);
      }
    }
    logger.debug("Finished generation of elements by resolver {}", resolver);
  }

  private void handleRow(Row row) {
    if (notInNestedLoop()) {
      // We can operate on original row
      excelWriter.setRow(row);
    } else {
      // We need to insert a new row
      excelWriter.shiftRows(row.getRowNum(), 1); // shift rows below insertion point one down, so we do not overwrite an existing one
      excelWriter.newRow(row);
    }
    excelWriter.addRowToIgnore(row.getRowNum());
    excelWriter.updateRowsWritten(1);
    ModificationInformation modificationInformation = new ModificationInformation(Optional.empty(), 0);
    for (Cell cell : row) {
      Optional<Integer> skipUntil = modificationInformation.skipUntil();
      if (skipUntil.isEmpty() || cell.getColumnIndex() > skipUntil.get()) {
        if (ExcelUtils.containsPlaceholder(cell)) {
          var newModificationInformation = replacePlaceholder(cell, modificationInformation.offset());
          modificationInformation = modificationInformation.merge(newModificationInformation);
        } else if (nestedLoopDepth != 0) {
          excelWriter.addCell(cell);
        }
      }
    }
  }

  private ModificationInformation replacePlaceholder(Cell cell, int offset) {
    String cellValue = ExcelUtils.getCellContentAsString(cell);
    Optional<PlaceholderData> placeholderDataOptional = ExcelUtils.resolveCell(cellValue, resolver);
    if (placeholderDataOptional.isPresent()) {
      PlaceholderData placeholderData = placeholderDataOptional.get();
      if (placeholderData instanceof ScalarPlaceholderData<?> scalarPlaceholderData
          && scalarPlaceholderData.getRawValue() instanceof Number number) {
        if(nestedLoopDepth > 0)
          excelWriter.addCell(cell, number.doubleValue());
        else // if we are not in a loop, we can set the cell value directly, otherwise we would overwrite the template cell and styles (*)
          cell.setCellValue(number.doubleValue());
        return ModificationInformation.empty();
      } else if (placeholderData.getType().equals(PlaceholderType.CUSTOM) && placeholderData instanceof ExcelPlaceholderData excelPlaceholderData) {
        return excelPlaceholderData.transform(cell, excelWriter, offset, LocaleUtil.getUserLocale(), options);
      }
    }
    // to resolve cell content such as "{{name}} {{crew}}", we match against the full string and resolve per match
    var matcher = ParsingUtils.matchPlaceholders(cellValue);
    String newCellText = matcher.replaceAll(matchResult -> resolver.resolve(matchResult.group(1))
            .orElse(new ScalarPlaceholderData<>(""))
            .toString());
    if(nestedLoopDepth > 0)
      excelWriter.addCell(cell, newCellText);
    else // (*) see above
      cell.setCellValue(newCellText);
    return ModificationInformation.empty();
  }

  private List<Row> handleLoop(Row row, List<Row> rows) {
    logger.debug("Handling loop at row {}", row.getRowNum());
    var loopBody = getLoopBody(row, rows);
    var loopBodySize = getLoopBodySize(loopBody);
    logger.debug("Loop body size: {}", loopBodySize);
    int loopSize = getLoopSize(loopBody);
    excelWriter.addRowToIgnore(row.getRowNum()); // ignore opening tag
    excelWriter.addRowToIgnore(loopBody.get(loopBody.size() - 1).getRowNum()); // ignore closing tag
    if (notInNestedLoop()) {
      // Insert all data after the template rows
      excelWriter.setSectionOffset(loopSize);
    }
    var loopBodyWithoutTags = loopBody.subList(1, loopBody.size() - 1);  // remove loop opening and closing tag
    PlaceholderData placeholderData = getPlaceholderData(row);
    placeholderData.stream().forEach(placeholderResolver ->
        ExcelGenerator.apply(placeholderResolver, loopBodyWithoutTags, excelWriter, nestedLoopDepth + 1, options));
    if (notInNestedLoop()) {
      // Processing of the outermost loop has finished, we can delete the template
      int rowNum = row.getRowNum();
      excelWriter.finishLoopProcessing(rowNum, loopSize);
      rows = rows.stream().filter(row1 -> {
        try {
          row1.toString();
          return true;
        } catch (XmlValueDisconnectedException ignored) {
          return false;
        }
      }).toList();
    } else {
      // We finished a nested loop, remove the template from the working set to continue processing of the current iteration
      rows = rows.subList(loopBodyWithoutTags.size() + 1, rows.size());
    }
    return rows;
  }

  private boolean notInNestedLoop() {
    return nestedLoopDepth == 0;
  }

  private List<Row> getLoopBody(Row row, List<Row> rows) {
    var placeholder = ExcelUtils.getPlaceholder(row);
    logger.debug("Getting loop body of {}", placeholder);
    LinkedList<Row> rowBuffer = new LinkedList<>();
    rowBuffer.add(row);
    Iterator<Row> rowIterator = rows.iterator();
    var rowInFocus = rowIterator.next();
    while (!ExcelUtils.isMatchingLoopEnd(rowInFocus, placeholder)) {
      rowBuffer.addLast(rowInFocus);
      rowInFocus = rowIterator.next();
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
    logger.debug("Resolving placeholder of {}", placeholder);
    return resolver
        .resolve(placeholder)
        .filter(p -> p.getType() == PlaceholderType.SET)
        .orElseThrow();
  }


  private boolean isLoopStart(Row row) {
    if (ExcelUtils.getNumberOfNonEmptyCells(row) == 1) {
      var cell = row.getCell(row.getFirstCellNum());
      if (cell.getCellType() == CellType.STRING) {
        var placeholderName = ParsingUtils.stripBrackets(
            cell.getStringCellValue()
        );
        return resolver.resolve(placeholderName)
            .filter(placeholderData -> placeholderData.getType() == PlaceholderType.SET)
            .map(placeholderData -> {
              var endLoopMarkers = ParsingUtils.getMatchingLoopEnds(placeholderName);
              // Since ExcelGenerator only takes an Iterator of the remaining elements, we can't
              // do a lookahead from that. So we access the sheet from the current row and create
              // a new iterator dropping everything before the current row.
              return StreamSupport.stream(row.getSheet().spliterator(), false)
                  // since we only get the physical row index we need to manually probe till we
                  // reach the current iterator state
                  .dropWhile(nextRow -> !nextRow.equals(row))
                  .skip(1)
                  .filter(nextRow -> ExcelUtils.getNumberOfNonEmptyCells(nextRow) == 1)
                  .map(nextRow -> nextRow.getCell(nextRow.getFirstCellNum()))
                  .filter(nextCell -> nextCell.getCellType() == CellType.STRING)
                  .map(Cell::getStringCellValue)
                  .anyMatch(text -> endLoopMarkers.contains(text.strip().toLowerCase()));
            }).orElse(false);
      }
    }
    return false;
  }
}
