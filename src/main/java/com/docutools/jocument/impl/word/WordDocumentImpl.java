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
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

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
      var locale = WordUtilities.getDocumentLanguage(document).orElse(Locale.getDefault());
      LocaleUtil.setUserLocale(locale);
      logger.info("Set user locale to {}", locale);

      List<IBodyElement> bodyElements = new ArrayList<>(document.getBodyElements().size());
      bodyElements.addAll(document.getBodyElements());

      logger.debug("Retrieved all body elements, starting WordGenerator");
      WordGenerator.apply(resolver, bodyElements, options);

      try (OutputStream os = Files.newOutputStream(file)) {
        logger.info("Writing document to {}", os);
        document.write(os);
      }
    }
    logger.info("Finished generation");
    return file;
  }

}
