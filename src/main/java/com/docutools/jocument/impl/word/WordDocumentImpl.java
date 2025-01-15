package com.docutools.jocument.impl.word;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class WordDocumentImpl extends DocumentImpl {
  private static final Logger logger = LogManager.getLogger();

  public WordDocumentImpl(Template template, PlaceholderResolver resolver, GenerationOptions options) {
    super(template, resolver, options);
  }

  @Override
  protected Path generate() throws IOException {
    logger.info("Starting generation");
    Path file = Files.createTempFile("jocument-", ".docx");
    try (XWPFDocument document = new XWPFDocument(template.openStream())) {
      List<IBodyElement> bodyElements = new ArrayList<>(document.getBodyElements().size() + document.getHeaderList().size());
      bodyElements.addAll(document.getBodyElements());
      bodyElements.addAll(document.getHeaderList().stream().flatMap(xwpfHeader -> xwpfHeader.getBodyElements().stream()).toList());
      bodyElements.addAll(document.getFooterList().stream().flatMap(xwpfFooter -> xwpfFooter.getBodyElements().stream()).toList());

      logger.debug("Retrieved all body elements, starting WordGenerator");
      WordGenerator.apply(resolver, bodyElements, options);

      cleanLastEmptyPage(document);

      document.enforceUpdateFields();

      try (OutputStream os = Files.newOutputStream(file)) {
        logger.info("Writing document to {}", os);
        document.write(os);
      }
    }
    logger.info("Finished generation");
    return file;
  }

  private void cleanLastEmptyPage(XWPFDocument document) {
    List<IBodyElement> elements = document.getBodyElements();
    int elementsToRemove = 0;
    for (int i = elements.size() - 1; i >= 0; i--) {
      IBodyElement element = elements.get(i);
      if (element instanceof XWPFParagraph xwpfParagraph && isEmpty(xwpfParagraph)) {
        elementsToRemove++;
      } else {
        break;
      }
    }
    for (; elementsToRemove > 0; elementsToRemove--) {
      document.removeBodyElement(elements.size() - 1);
    }
  }

  private boolean isEmpty(XWPFParagraph xwpfParagraph) {
    return xwpfParagraph.isPageBreak() || (xwpfParagraph.getText().trim().isEmpty() && !hasPictures(xwpfParagraph));
  }

  private boolean hasPictures(XWPFParagraph paragraph) {
    for (XWPFRun xwpfRun : paragraph.getRuns()) {
      if (!xwpfRun.getEmbeddedPictures().isEmpty()) {
        return true;
      }
    }
    return false;
  }
}
