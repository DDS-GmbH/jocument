package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.ParsingUtils;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.util.ExcelUtils;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.LocaleUtil;


/**
 * This class is responsible for creating excel report rows from template rows. It is used recursively when resolving nested loops. Because of this,
 * the generator is agnostic of enclosing structures like sheets and workbooks.
 *
 * @author Anton Oellerer
 * @since 2020-04-10
 */
public class ExcelGenerator {
  private static final Logger logger = LogManager.getLogger();

  private final ExcelWriter excelWriter;
  private final PlaceholderResolver resolver;
  private final Iterator<Row> rowIterator;
  private final int nestedLoopDepth;
  private final GenerationOptions options;
  private int alreadyProcessedLoopsSize = 0;

  private ExcelGenerator(Iterator<Row> rowIterator, ExcelWriter excelWriter, PlaceholderResolver resolver, int nestedLoopDepth,
                         GenerationOptions options) {
    this.rowIterator = rowIterator;
    this.excelWriter = excelWriter;
    this.resolver = resolver;
    this.nestedLoopDepth = nestedLoopDepth;
    this.options = options;
  }

  /**
   * This function starts the generating process for the supplied row iterator.
   *
   * @param resolver    The resolver to use for looking up placeholders
   * @param rowIterator An iterator over the template row which should be processed
   * @param excelWriter The writer to write the report out to.
   * @param options     {@link GenerationOptions}
   */
  static void apply(PlaceholderResolver resolver, Iterator<Row> rowIterator, ExcelWriter excelWriter, GenerationOptions options) {
    apply(resolver, rowIterator, excelWriter, 0, options);
  }

  private static void apply(PlaceholderResolver resolver, Iterator<Row> rowIterator, ExcelWriter excelWriter, int nestedLoopDepth,
                            GenerationOptions options) {
    new ExcelGenerator(rowIterator, excelWriter, resolver, nestedLoopDepth, options).generate();
  }

  private void generate() {
    logger.debug("Starting generation by applying resolver {}", resolver);
    for (Iterator<Row> iterator = rowIterator; iterator.hasNext(); ) {
      Row row = iterator.next();
      if (isCustomPlaceholder(row)) {
        var cell = row.getCell(row.getFirstCellNum());
        resolver.resolve(ParsingUtils.stripBrackets(
                cell.getStringCellValue()
            ))
            .ifPresent(placeholderData -> placeholderData.transform(row, excelWriter, LocaleUtil.getUserLocale(), options));
      } else if (isLoopStart(row)) {
        handleLoop(row, iterator);
      } else {
        excelWriter.newRow(row);
        for (Cell cell : row) {
          if (ExcelUtils.containsPlaceholder(cell)) {
            var newCellText = ExcelUtils.replacePlaceholders(cell, resolver);
            excelWriter.addCell(cell, newCellText);
          } else if (ExcelUtils.isSimpleCell(cell)) {
            excelWriter.addCell(cell);
          }
        }
      }
    }
    if (nestedLoopDepth != 0) { //here for clarity, could be removed since generation finishes if nestedLoopDepth == 0
      logger.debug("Adding offset of {}", alreadyProcessedLoopsSize);
      excelWriter.addRowOffset(alreadyProcessedLoopsSize); //we are in nested loop, readd the offset to prevent subtracting it multiple times
    }
    logger.debug("Finished generation of elements by resolver {}", resolver);
  }

  private void handleLoop(Row row, Iterator<Row> iterator) {
    logger.debug("Handling loop at row {}", row.getRowNum());
    var loopBody = getLoopBody(row, iterator);
    var loopBodySize = getLoopBodySize(loopBody);
    logger.debug("Loop body size: {}", loopBodySize);
    var finalLoopBody = loopBody.subList(1, loopBody.size() - 1);
    var placeholderData = getPlaceholderData(row);
    placeholderData.stream()
        .forEach(placeholderResolver -> {
          excelWriter.addRowOffset(-1); //So we also fill the cell of the loop start placeholder
          ExcelGenerator.apply(placeholderResolver, finalLoopBody.iterator(), excelWriter, nestedLoopDepth + 1, options);
          excelWriter.addRowOffset(1); //To avoid subtracting the placeholder size multiple times
          excelWriter.addRowOffset(loopBodySize);
        });
    var loopPlaceholderSize = getLoopSize(loopBody);
    excelWriter.addRowOffset(-1 * loopPlaceholderSize);
    logger.debug("Subtracting row offset of {}", loopPlaceholderSize);
    alreadyProcessedLoopsSize += loopPlaceholderSize;
  }

  private List<Row> getLoopBody(Row row, Iterator<Row> iterator) {
    var placeholder = ExcelUtils.getPlaceholder(row);
    logger.debug("Unrolling loop of {}", placeholder);
    LinkedList<Row> rowBuffer = new LinkedList<>();
    rowBuffer.add(row);
    var rowInFocus = iterator.next();
    while (!ExcelUtils.isMatchingLoopEnd(rowInFocus, placeholder)) {
      rowBuffer.addLast(rowInFocus);
      rowInFocus = iterator.next();
    }
    rowBuffer.addLast(rowInFocus);
    logger.debug("Unrolled loop of {}", placeholder);
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

  private boolean isCustomPlaceholder(Row row) {
    var firstCell = row.getFirstCellNum();
    if (firstCell < 0) {
      return false;
    }
    var cell = row.getCell(firstCell);
    if (cell.getCellType() == CellType.STRING) {
      return resolver.resolve(
              ParsingUtils.stripBrackets(
                  cell.getStringCellValue()
              )).map(PlaceholderData::getType)
          .map(type -> type == PlaceholderType.CUSTOM)
          .orElse(false);
    }
    return false;
  }
}
