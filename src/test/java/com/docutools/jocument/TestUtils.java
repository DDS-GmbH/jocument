package com.docutools.jocument;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public final class TestUtils {

  private TestUtils() {
  }

  public static String getText(String resName) throws IOException {
    try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resName)) {
      assert inputStream != null;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        return reader.lines()
            .collect(Collectors.joining());
      }
    }
  }

  public static XSSFWorkbook getXSSFWorkbookFromDocument(Document document) throws IOException {
    return new XSSFWorkbook(document.getPath().toString());
  }

  public static XWPFDocument getXWPFDocumentFromDocument(Document document) throws IOException {
    return new XWPFDocument(new BufferedInputStream(new FileInputStream(document.getPath().toFile())));
  }
}
