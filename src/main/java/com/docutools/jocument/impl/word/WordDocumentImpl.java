package com.docutools.jocument.impl.word;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class WordDocumentImpl extends DocumentImpl {

  public WordDocumentImpl(Template template, PlaceholderResolver resolver) {
    super(template, resolver);
  }

  @Override
  protected Path generate() throws IOException {
    Path file = Files.createTempFile("document", ".docx");
    try (XWPFDocument document = new XWPFDocument(template.openStream())) {

      List<IBodyElement> bodyElements = new ArrayList<>(document.getBodyElements().size());
      bodyElements.addAll(document.getBodyElements());

      WordGenerator.apply(resolver, bodyElements);

      try (OutputStream os = Files.newOutputStream(file)) {
        document.write(os);
      }
    }
    return file;
  }

}
