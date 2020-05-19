package com.docutools.jocument.impl;

import com.docutools.jocument.Document;
import com.docutools.jocument.MimeType;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.excel.implementations.ExcelDocumentImpl;
import com.docutools.jocument.impl.word.WordDocumentImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class TemplateImpl implements Template {
    private static final Logger logger = LogManager.getLogger();

  private final URL url;
  private final MimeType mimeType;
  private final Locale locale;

  public TemplateImpl(URL url, MimeType mimeType, Locale locale) {
    this.url = url;
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
  public Document startGeneration(PlaceholderResolver resolver) {
      logger.info("Starting generating from template {} with resolver {}", this, resolver);
      var document = switch (mimeType) {
          case DOCX -> new WordDocumentImpl(this, resolver);
          case XLSX -> new ExcelDocumentImpl(this, resolver);
      };
      document.start();
      logger.info("Finished generating from template {} with resolver {}", this, resolver);
      return document;
  }

  @Override
  public InputStream openStream() throws IOException {
    return url.openStream();
  }
}
