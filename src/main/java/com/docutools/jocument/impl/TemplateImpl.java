package com.docutools.jocument.impl;

import com.docutools.jocument.Document;
import com.docutools.jocument.MimeType;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.word.WordDocumentImpl;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class TemplateImpl implements Template {

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
    var document = new WordDocumentImpl(this, resolver);
    document.start();
    return document;
  }

  @Override
  public InputStream openStream() throws IOException {
    return url.openStream();
  }
}
