package com.docutools.jocument.impl.word;

import org.apache.poi.xwpf.usermodel.IBodyElement;
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

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WordUtilities {

  public static String toString(XWPFParagraph paragraph) {
    return getText(paragraph)
            .orElseGet(() -> joinRuns(paragraph));
  }

  public static void replaceText(XWPFParagraph paragraph, String newText) {
    List<XWPFRun> runs = paragraph.getRuns();
    if(runs.isEmpty()) {
      XWPFRun run = paragraph.createRun();
      run.setText(newText, 0);
    } else {
      runs.get(0).setText(newText, 0);
      IntStream.range(1, runs.size()).forEach(paragraph::removeRun);
    }
  }

  public static boolean exists(IBodyElement element) {
    return findPos(element).orElse(-1) != -1;
  }

  public static OptionalInt findPos(IBodyElement element) {
    var document = element.getBody().getXWPFDocument();
    if(element instanceof XWPFParagraph) {
      return OptionalInt.of(document.getPosOfParagraph((XWPFParagraph)element));
    } else if(element instanceof XWPFTable) {
      return OptionalInt.of(document.getPosOfTable((XWPFTable)element));
    }
    return OptionalInt.empty();
  }

  public static List<IBodyElement> copyBefore(List<IBodyElement> elements, IBodyElement destination) {
    return elements.stream()
            .map(element -> copyBefore(element, destination))
            .collect(Collectors.toList());
  }

  public static IBodyElement copyBefore(IBodyElement element, IBodyElement destination) {
    var destinationCursor = openCursor(destination).orElseThrow();

    if(element instanceof XWPFTable) {
      return copyTableTo((XWPFTable)element, destinationCursor);
    }
    if(element instanceof XWPFParagraph) {
      return copyParagraphTo((XWPFParagraph)element, destinationCursor);
    }

    throw new IllegalArgumentException("Can only copy XWPFParagraph or XWPFTable instances.");
  }

  public static void removeIfExists(IBodyElement element) {
    findPos(element)
            .ifPresent(element.getBody().getXWPFDocument()::removeBodyElement);
  }

  public static Optional<XmlCursor> openCursor(IBodyElement element) {
    if(element instanceof XWPFParagraph) {
      return Optional.of(((XWPFParagraph)element).getCTP().newCursor());
    } else if(element instanceof XWPFTable) {
      return Optional.of(((XWPFTable)element).getCTTbl().newCursor());
    } else {
      return Optional.empty();
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
    if(rawText != null && !rawText.isBlank()) {
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
    if(tblPr == null) {
      tblPr = tbl.addNewTblPr();
    }
    tblPr.set(original.getCTTbl().getTblPr());
    clone.removeRow(0);
    original.getRows()
            .forEach(row -> {
              XWPFTableRow newRow = clone.createRow();
              CTRow ctRow = newRow.getCtRow();
              CTTrPr pr = ctRow.getTrPr();
              if(pr == null) {
                pr = ctRow.addNewTrPr();
              }
              pr.set(row.getCtRow().getTrPr());
              List<XWPFTableCell> cells = row.getTableCells();
              for(int i = 0; i < cells.size(); i++) {
                XWPFTableCell cell = cells.get(i);
                XWPFTableCell newCell = newRow.getCell(i) != null? newRow.getCell(i) : newRow.createCell();
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
    CTPPr pPr = clone.getCTP().isSetPPr() ?
            clone.getCTP().getPPr() : clone.getCTP().addNewPPr();
    pPr.set(original.getCTP().getPPr());
    for (XWPFRun r : original.getRuns()) {
      XWPFRun nr = clone.createRun();
      cloneRun(r, nr);
    }
  }

  private static void cloneRun(XWPFRun original, XWPFRun clone) {
    CTRPr rPr = clone.getCTR().isSetRPr() ? clone.getCTR().getRPr() : clone.getCTR().addNewRPr();
    rPr.set(original.getCTR().getRPr());
    String text = original.getText(0);
    clone.setText(text != null ? text : "");
    // TODO copy images in XWPFRun
  }

  private WordUtilities() {
  }

}
