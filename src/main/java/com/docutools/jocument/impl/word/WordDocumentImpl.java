package com.docutools.jocument.impl.word;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PostProcessor;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WordDocumentImpl extends DocumentImpl {

  private PostProcessor<XWPFDocument> postProcessor;

  public WordDocumentImpl(Template template, PlaceholderResolver resolver) {
    super(template, resolver);
  }

  public WordDocumentImpl(Template template, PlaceholderResolver resolver, PostProcessor<XWPFDocument> postProcessor) {
    super(template, resolver);
    this.postProcessor = postProcessor;
  }

  @Override
  protected Path generate() throws IOException {
    Path file = Files.createTempFile("document", ".docx");
    try (XWPFDocument document = new XWPFDocument(template.openStream())) {
      LocaleUtil.setUserLocale(WordUtilities.detectMostCommonLocale(document).orElse(Locale.getDefault()));
      List<IBodyElement> bodyElements = new ArrayList<>(document.getBodyElements().size());
      bodyElements.addAll(document.getBodyElements());

      WordGenerator.apply(resolver, bodyElements);

      if (postProcessor != null) {
        postProcessor.process(document, resolver);
      }

      try (OutputStream os = Files.newOutputStream(file)) {
        document.write(os);
      }
    }
    return file;
  }

}
