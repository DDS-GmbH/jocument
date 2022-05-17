package com.docutools.jocument.impl;

import com.docutools.jocument.Document;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.MimeType;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.TemplateSource;
import com.docutools.jocument.impl.excel.implementations.ExcelDocumentImpl;
import com.docutools.jocument.impl.word.WordDocumentImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TemplateImpl implements Template {
  private static final Logger logger = LogManager.getLogger();

  private final TemplateSource source;
  private final MimeType mimeType;
  private final Locale locale;

  /**
   * Create a new template object from {@link TemplateSource} {@code source}.
   *
   * @param source   The source to create the template from
   * @param mimeType The mime type of the template and the document to generate
   * @param locale   The locale to use when generating the report
   */
  public TemplateImpl(TemplateSource source, MimeType mimeType, Locale locale) {
    this.source = source;
    this.mimeType = mimeType;
    this.locale = locale;
  }

  @Override
  public MimeType getMimeType() {
    return mimeType;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public Document startGeneration(PlaceholderResolver resolver, GenerationOptions options) {
    // Make sure PlaceholderResolver and Template work on the same GenerationOptions.
    resolver.setOptions(options);

    logger.info("Starting generating from template {} with resolver {}", this, resolver);
    var document = switch (mimeType) {
      case DOCX -> new WordDocumentImpl(this, resolver, options);
      case XLSX -> new ExcelDocumentImpl(this, resolver, options);
    };
    document.start();
    return document;
  }

  @Override
  public InputStream openStream() throws IOException {
    return source.open();
  }
}
