package com.docutools.jocument.impl.excel;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.excel.implementations.ExcelGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExcelDocumentImpl extends DocumentImpl {

  public ExcelDocumentImpl(Template template, PlaceholderResolver resolver) {
    super(template, resolver);
  }

  @Override
  protected Path generate() throws IOException {
    Path outFile = Files.createTempFile("document", ".xlsx");
    ExcelGenerator excelGenerator = new ExcelGenerator(template, outFile);
    excelGenerator.generate();
    return outFile;

  }
}
