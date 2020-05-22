package com.docutools.jocument.impl.word;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.postprocessing.PostProcessingAdministrator;
import com.docutools.jocument.postprocessing.impl.PostProcessingAdministratorImpl;
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

  private final PostProcessingAdministrator<XWPFDocument> postProcessingAdministrator;

  public WordDocumentImpl(Template template, PlaceholderResolver resolver) {
    super(template, resolver);
    this.postProcessingAdministrator = new PostProcessingAdministratorImpl<>(); //NoOp postprocessor
  }

  public WordDocumentImpl(Template template, PlaceholderResolver resolver, PostProcessingAdministrator<XWPFDocument> postProcessingAdministrator) {
    super(template, resolver);
    this.postProcessingAdministrator = postProcessingAdministrator;
  }

  @Override
  protected Path generate() throws IOException {
    Path file = Files.createTempFile("document", ".docx");
    try (XWPFDocument document = new XWPFDocument(template.openStream())) {
      LocaleUtil.setUserLocale(WordUtilities.detectMostCommonLocale(document).orElse(Locale.getDefault()));
      List<IBodyElement> bodyElements = new ArrayList<>(document.getBodyElements().size());
      bodyElements.addAll(document.getBodyElements());

      WordGenerator.apply(resolver, bodyElements);

      postProcessingAdministrator.process(document, resolver);

      try (OutputStream os = Files.newOutputStream(file)) {
        document.write(os);
      }
    }
    return file;
  }

}
