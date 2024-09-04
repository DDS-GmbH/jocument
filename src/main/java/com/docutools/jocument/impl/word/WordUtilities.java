package com.docutools.jocument.impl.word;

import com.docutools.jocument.impl.ParsingUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;

public class WordUtilities {
  private static final Logger logger = LogManager.getLogger();

  private WordUtilities() {
  }

  /**
   * Joins all {@link org.apache.poi.xwpf.usermodel.XWPFRun}s of this {@link org.apache.poi.xwpf.usermodel.XWPFParagraph}
   * to one string.
   *
   * @param paragraph the {@link org.apache.poi.xwpf.usermodel.XWPFParagraph}
   * @return the joined String
   */
  public static String toString(XWPFParagraph paragraph) {
    return getText(paragraph)
        .orElseGet(() -> joinRuns(paragraph));
  }

  /**
   * Replaces the text in the {@link org.apache.poi.xwpf.usermodel.XWPFParagraph} without loosing formatting.
   *
   * @param paragraph the {@link org.apache.poi.xwpf.usermodel.XWPFParagraph}
   * @param newText   the new test
   */
  public static void replaceText(XWPFParagraph paragraph, String newText) {
    List<XWPFRun> runs = paragraph.getRuns();
    if (runs.isEmpty()) {
      XWPFRun run = paragraph.createRun();
      run.setText(newText, 0);
    } else {
      runs.get(0).setText(newText, 0);
      // When deleting a run from a paragraph, the collection keeping the runs shrinks to fit to the new size
      // If we delete the runs with indices 1,2,3...,x,  the second half of the delete operations fails silently
      // To avoid this, we simply delete the first run x times.
      IntStream.range(1, runs.size()).forEach(value -> paragraph.removeRun(1));
    }
  }

  /**
   * Tests if the given element is still part of the referenced {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element
   * @return {@code true} when exists
   */
  public static boolean exists(IBodyElement element) {
    return findPositionInBody(element).isPresent() || findInHeader(element) || findInFooter(element) || findNestedInTable(element);
  }

  /**
   * Tries to find the position index of the given element in its {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element
   * @return the index
   */
  public static Optional<Integer> findPositionInBody(IBodyElement element) {
    int index = element.getBody().getXWPFDocument().getBodyElements().indexOf(element);
    return index == -1 ? Optional.empty() : Optional.of(index);
  }

  /**
   * Copies the given elements before the destination element.
   *
   * @param elements    the elements to copy
   * @param destination the destination
   * @return the copies elements
   */
  public static List<IBodyElement> copyBefore(List<IBodyElement> elements, IBodyElement destination) {
    return elements.stream()
        .map(element -> copyBefore(element, destination))
        .toList();
  }

  /**
   * Copies the given element before the destination.
   *
   * @param element     the element
   * @param destination the destination
   * @return the copies element
   */
  public static IBodyElement copyBefore(IBodyElement element, IBodyElement destination) {
    var destinationCursor = openCursor(destination).orElseThrow();

    if (element instanceof XWPFTable xwpfTable) {
      return copyTableTo(xwpfTable, destinationCursor);
    }
    if (element instanceof XWPFParagraph xwpfParagraph) {
      return copyParagraphTo(xwpfParagraph, destinationCursor);
    }
    logger.error("Failed to copy {} before {}", element, destination);
    throw new IllegalArgumentException("Can only copy XWPFParagraph or XWPFTable instances.");
  }

  /**
   * Removes the element if it still exists in the referenced {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element to be removed
   */
  public static void removeIfExists(IBodyElement element) {
    IBody body = element.getBody();
    XWPFDocument xwpfDocument = body.getXWPFDocument();
    CTDocument1 document = xwpfDocument.getDocument();
    if (element instanceof XWPFParagraph xwpfParagraph) {
      try (XmlCursor xmlCursor = xwpfParagraph.getCTP().newCursor()) {
        XmlObject object = getParentObject(xmlCursor);
        if (object.equals(document.getBody())) {
          findPositionInBody(element).ifPresent(xwpfDocument::removeBodyElement);
        } else if (object instanceof CTTc) {
          removeElementFromTable(xwpfParagraph);
        } else if (object instanceof CTHdrFtr ctHdrFtr) {
          removeObjectFromDocument(ctHdrFtr, xwpfDocument, xwpfHeaderFooter -> xwpfHeaderFooter.removeParagraph(xwpfParagraph));
        }
      }
    } else if (element instanceof XWPFTable xwpfTable) {
      try (XmlCursor xmlCursor = xwpfTable.getCTTbl().newCursor()) {
        XmlObject object = getParentObject(xmlCursor);
        if (object.equals(document.getBody())) {
          findPositionInBody(element).ifPresent(xwpfDocument::removeBodyElement);
        } else if (object instanceof CTTc) {
          removeElementFromTable(xwpfTable);
        } else if (object instanceof CTHdrFtr ctHdrFtr) {
          removeObjectFromDocument(ctHdrFtr, xwpfDocument, xwpfHeaderFooter -> xwpfHeaderFooter.removeTable(xwpfTable));
        }
      }
    }
  }

  private static void removeObjectFromDocument(CTHdrFtr ctHdrFtr, XWPFDocument xwpfDocument, Consumer<XWPFHeaderFooter> removalAction) {
    List<XWPFHeaderFooter> headerFooterList = new LinkedList<>(xwpfDocument.getHeaderList());
    headerFooterList.addAll(xwpfDocument.getFooterList());
    for (XWPFHeaderFooter xwpfHeaderFooter : headerFooterList) {
      if (xwpfHeaderFooter._getHdrFtr().equals(ctHdrFtr)) {
        removalAction.accept(xwpfHeaderFooter);
        break;
      }
    }
  }

  private static XmlObject getParentObject(XmlCursor xmlCursor) {
    xmlCursor.toParent();
    return xmlCursor.getObject();
  }

  private static void removeElementFromTable(XWPFParagraph xwpfParagraph) {
    while (!xwpfParagraph.runsIsEmpty()) {
      xwpfParagraph.removeRun(0);
    }
  }

  private static void removeElementFromTable(XWPFTable xwpfTable) {
    for (int i = 0; i < xwpfTable.getNumberOfRows(); i++) {
      xwpfTable.removeRow(0);
    }
  }

  private static boolean findInHeader(IBodyElement element) {
    return findInHeader(element, element.getBody().getXWPFDocument().getHeaderList());
  }

  private static boolean findInHeader(IBodyElement element, List<XWPFHeader> headers) {
    for (XWPFHeader header : headers) {
      for (IBodyElement bodyElement : header.getBodyElements()) {
        if (element.equals(bodyElement)) {
          return true;
        }
      }
      if (findInTables(element, header.getTables())) {
        return true;
      }
    }
    return false;
  }

  private static boolean findInFooter(IBodyElement element) {
    return findInFooter(element, element.getBody().getXWPFDocument().getFooterList());
  }

  private static boolean findInFooter(IBodyElement element, List<XWPFFooter> footers) {
    for (XWPFFooter footer : footers) {
      for (IBodyElement bodyElement : footer.getBodyElements()) {
        if (element.equals(bodyElement)) {
          return true;
        }
      }
      if (findInTables(element, footer.getTables())) {
        return true;
      }
    }
    return false;
  }

  private static boolean findNestedInTable(IBodyElement element) {
    for (XWPFTable table : element.getBody().getXWPFDocument().getTables()) {
      if (findInTable(element, table)) {
        return true;
      }
    }
    return false;
  }

  private static boolean findInTable(IBodyElement element, XWPFTable table) {
    for (XWPFTableRow row : table.getRows()) {
      if (findInRow(element, row)) {
        return true;
      }
    }
    return false;
  }

  private static boolean findInRow(IBodyElement element, XWPFTableRow row) {
    for (XWPFTableCell cell : row.getTableCells()) {
      if (findInCell(element, cell)) {
        return true;
      }
    }
    return false;
  }

  private static boolean findInCell(IBodyElement element, XWPFTableCell cell) {
    if (element instanceof XWPFParagraph && findInParagraphs(element, cell.getParagraphs())) {
      return true;
    }
    return findInTables(element, cell.getTables());
  }

  private static boolean findInParagraphs(IBodyElement element, List<XWPFParagraph> paragraphs) {
    for (XWPFParagraph paragraph : paragraphs) {
      if (element.equals(paragraph)) {
        return true;
      }
    }
    return false;
  }

  private static boolean findInTables(IBodyElement element, List<XWPFTable> tables) {
    for (XWPFTable nestedTable : tables) {
      if ((element instanceof XWPFTable && element.equals(nestedTable)) || findInTable(element, nestedTable)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Opens a {@link org.apache.xmlbeans.XmlCursor} to the given element in its {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element
   * @return the cursor
   */
  public static Optional<XmlCursor> openCursor(IBodyElement element) {
    if (element instanceof XWPFParagraph xwpfParagraph) {
      logger.debug("Opening cursor to paragraph {}", xwpfParagraph);
      return Optional.of(xwpfParagraph.getCTP().newCursor());
    } else if (element instanceof XWPFTable xwpfTable) {
      logger.debug("Opening cursor to table {}", xwpfTable);
      return Optional.of(xwpfTable.getCTTbl().newCursor());
    } else {
      logger.warn("Failed to open cursor to element {}", element);
      return Optional.empty();
    }
  }

  private static XWPFTable copyTableTo(XWPFTable sourceTable, XmlCursor cursor) {
    logger.debug("Copying table {} before {}", sourceTable, cursor);
    var document = sourceTable.getBody().getXWPFDocument();
    XWPFTable table = document.insertNewTbl(cursor);
    cloneTable(sourceTable, table);
    return table;
  }

  private static XWPFParagraph copyParagraphTo(XWPFParagraph sourceParagraph, XmlCursor cursor) {
    logger.debug("Copying paragraph {} before {}", sourceParagraph, cursor);
    var document = sourceParagraph.getDocument();
    XWPFParagraph paragraph = document.insertNewParagraph(cursor);
    cloneParagraph(sourceParagraph, paragraph);
    return paragraph;
  }

  private static Optional<String> getText(XWPFParagraph paragraph) {
    var rawText = paragraph.getText();
    if (rawText != null && !rawText.isBlank()) {
      return Optional.of(rawText);
    }
    logger.debug("Paragraph {} did not contain any text", paragraph);
    return Optional.empty();
  }

  private static String joinRuns(XWPFParagraph paragraph) {
    return paragraph.getRuns().stream()
        .map(run -> run.getText(0))
        .filter(Objects::nonNull)       // if run is "", run.getText(0) returns null
        .collect(Collectors.joining());
  }

  private static void cloneTable(XWPFTable original, XWPFTable clone) {
    logger.debug("Cloning table {} to table {}", original, clone);
    CTTbl tbl = clone.getCTTbl();
    tbl.setTblGrid(original.getCTTbl().getTblGrid());
    CTTblPr tblPr = tbl.getTblPr();
    if (tblPr == null) {
      tblPr = tbl.addNewTblPr();
    }
    tblPr.set(original.getCTTbl().getTblPr());
    clone.removeRow(0);
    original.getRows()
        .forEach(row -> {
          XWPFTableRow newRow = clone.createRow();
          CTRow ctRow = newRow.getCtRow();
          CTTrPr pr = ctRow.getTrPr();
          if (pr == null) {
            pr = ctRow.addNewTrPr();
          }
          pr.set(row.getCtRow().getTrPr());
          List<XWPFTableCell> cells = row.getTableCells();
          for (int i = 0; i < cells.size(); i++) {
            XWPFTableCell cell = cells.get(i);
            XWPFTableCell newCell = newRow.getCell(i) != null ? newRow.getCell(i) : newRow.createCell();
            CTTc ctTc = newCell.getCTTc();
            CTTcPr ctTcPr = ctTc.getTcPr();
            if (ctTcPr == null) {
              ctTcPr = ctTc.addNewTcPr();
            }
            ctTcPr.set(cell.getCTTc().getTcPr());
            if (!newCell.getParagraphs().isEmpty()) {
              // The new cell might be created with empty paragraphs which we do not need, so we remove them here
              IntStream.range(0, newCell.getParagraphs().size()).forEach(value -> newCell.removeParagraph(0));
            }
            cell.getParagraphs()
                .forEach(paragraph -> {
                  XWPFParagraph newParagraph = newCell.addParagraph();
                  cloneParagraph(paragraph, newParagraph);
                });
          }
        });
  }

  private static void cloneParagraph(XWPFParagraph original, XWPFParagraph clone) {
    logger.debug("Cloning table {} to table {}", original, clone);
    CTPPr ppr = clone.getCTP().isSetPPr()
        ? clone.getCTP().getPPr() : clone.getCTP().addNewPPr();
    ppr.set(original.getCTP().getPPr());
    for (XWPFRun r : original.getRuns()) {
      XWPFRun nr = clone.createRun();
      cloneRun(r, nr);
    }
  }

  private static void cloneRun(XWPFRun original, XWPFRun clone) {
    logger.debug("Cloning run {} to run {}", original, clone);
    CTRPr rpr = clone.getCTR().isSetRPr() ? clone.getCTR().getRPr() : clone.getCTR().addNewRPr();
    rpr.set(original.getCTR().getRPr());
    String text = original.getText(0);
    clone.setText(text != null ? text : "");
  }

  public static String extractPlaceholderName(XWPFParagraph paragraph) {
    return ParsingUtils.stripBrackets(WordUtilities.toString(paragraph));
  }
}
