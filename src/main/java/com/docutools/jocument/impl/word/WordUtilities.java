package com.docutools.jocument.impl.word;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;

public class WordUtilities {

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
   * @param newText the new test
   */
  public static void replaceText(XWPFParagraph paragraph, String newText) {
    List<XWPFRun> runs = paragraph.getRuns();
    if (runs.isEmpty()) {
      XWPFRun run = paragraph.createRun();
      run.setText(newText, 0);
    } else {
      runs.get(0).setText(newText, 0);
      IntStream.range(1, runs.size()).forEach(paragraph::removeRun);
    }
  }

  /**
   * Tests if the given element is still part of the referenced {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element
   * @return {@code true} when exists
   */
  public static boolean exists(IBodyElement element) {
    return findPos(element).orElse(-1) != -1;
  }

  /**
   * Tries to find the position index of the given element in its {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element
   * @return the index
   */
  public static OptionalInt findPos(IBodyElement element) {
    var document = element.getBody().getXWPFDocument();
    if (element instanceof XWPFParagraph) {
      return OptionalInt.of(document.getPosOfParagraph((XWPFParagraph) element));
    } else if (element instanceof XWPFTable) {
      return OptionalInt.of(document.getPosOfTable((XWPFTable) element));
    }
    return OptionalInt.empty();
  }

  /**
   * Copies the given elements before the destination element.
   *
   * @param elements the elements to copy
   * @param destination the destination
   * @return the copies elements
   */
  public static List<IBodyElement> copyBefore(List<IBodyElement> elements, IBodyElement destination) {
    return elements.stream()
            .map(element -> copyBefore(element, destination))
            .collect(Collectors.toList());
  }

  /**
   * Copies the given element before the destination.
   *
   * @param element the element
   * @param destination the destination
   * @return the copies element
   */
  public static IBodyElement copyBefore(IBodyElement element, IBodyElement destination) {
    var destinationCursor = openCursor(destination).orElseThrow();

    if (element instanceof XWPFTable) {
      return copyTableTo((XWPFTable) element, destinationCursor);
    }
    if (element instanceof XWPFParagraph) {
      return copyParagraphTo((XWPFParagraph) element, destinationCursor);
    }

    throw new IllegalArgumentException("Can only copy XWPFParagraph or XWPFTable instances.");
  }

  /**
   * Removes the element if it still exists in the referenced {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element to be removed
   */
  public static void removeIfExists(IBodyElement element) {
    findPos(element)
            .ifPresent(element.getBody().getXWPFDocument()::removeBodyElement);
  }

  /**
   * Opens a {@link org.apache.xmlbeans.XmlCursor} to the given element in its {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param element the element
   * @return the cursor
   */
  public static Optional<XmlCursor> openCursor(IBodyElement element) {
    if (element instanceof XWPFParagraph) {
      return Optional.of(((XWPFParagraph) element).getCTP().newCursor());
    } else if (element instanceof XWPFTable) {
      return Optional.of(((XWPFTable) element).getCTTbl().newCursor());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns all languages used in {@link org.apache.poi.xwpf.usermodel.XWPFRun}s for out the given
   * {@link org.apache.poi.xwpf.usermodel.XWPFDocument}.
   *
   * @param document the document to parse
   * @return distinct languages as {@link java.util.Locale} instances
   */
  public static Collection<Locale> detectLanguages(XWPFDocument document) {
    var tableParagraphs = document.getTables()
            .stream()
            .flatMap(table -> getTableEmbeddedParagraphs(table).stream());

    var documentParagraphs = document.getParagraphs().stream();

    return Stream.concat(tableParagraphs, documentParagraphs)
            .flatMap(paragraph -> paragraph.getRuns().stream())
            .map(XWPFRun::getLang)
            .filter(Objects::nonNull)
            .distinct()
            .map(Locale::forLanguageTag)
            .filter(WordUtilities::isValid)
            .collect(Collectors.toList());
  }

  public static Collection<XWPFParagraph> getTableEmbeddedParagraphs(XWPFTable table) {
    var paragraphs = new LinkedList<XWPFParagraph>();
      for (XWPFTableRow row : table.getRows()) {
          for (XWPFTableCell cell : row.getTableCells()) {
            paragraphs.addAll(cell.getParagraphs());
            for (XWPFTable subtable : cell.getTables()) {
              paragraphs.addAll(getTableEmbeddedParagraphs(subtable));
            }
          }
      }
      return paragraphs;
  }

  private static boolean isValid(Locale locale) {
    //Taken from https://stackoverflow.com/a/3684832
    try {
      return locale.getISO3Language() != null && locale.getISO3Country() != null;
    } catch (MissingResourceException e) {
      return false;
    }
  }

  private static XWPFTable copyTableTo(XWPFTable sourceTable, XmlCursor cursor) {
    var document = sourceTable.getBody().getXWPFDocument();
    XWPFTable table = document.insertNewTbl(cursor);
    cloneTable(sourceTable, table);
    return table;
  }

  private static XWPFParagraph copyParagraphTo(XWPFParagraph sourceParagraph, XmlCursor cursor) {
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
    return Optional.empty();
  }

  private static String joinRuns(XWPFParagraph paragraph) {
    return paragraph.getRuns().stream()
            .map(run -> run.getText(0))
            .collect(Collectors.joining());
  }

  private static void cloneTable(XWPFTable original, XWPFTable clone) {
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
                newCell.removeParagraph(0);
                CTTc ctTc = newCell.getCTTc();
                CTTcPr ctTcPr = ctTc.getTcPr();
                if (ctTcPr == null) {
                  ctTcPr = ctTc.addNewTcPr();
                }
                ctTcPr.set(cell.getCTTc().getTcPr());
                cell.getParagraphs()
                        .forEach(paragraph -> {
                          XWPFParagraph newParagraph = newCell.addParagraph();
                          cloneParagraph(paragraph, newParagraph);
                        });
              }
            });
  }

  private static void cloneParagraph(XWPFParagraph original, XWPFParagraph clone) {
    CTPPr ppr = clone.getCTP().isSetPPr()
            ? clone.getCTP().getPPr() : clone.getCTP().addNewPPr();
    ppr.set(original.getCTP().getPPr());
    for (XWPFRun r : original.getRuns()) {
      XWPFRun nr = clone.createRun();
      cloneRun(r, nr);
    }
  }

  private static void cloneRun(XWPFRun original, XWPFRun clone) {
    CTRPr rpr = clone.getCTR().isSetRPr() ? clone.getCTR().getRPr() : clone.getCTR().addNewRPr();
    rpr.set(original.getCTR().getRPr());
    String text = original.getText(0);
    clone.setText(text != null ? text : "");
  }

}
