package com.docutools.jocument.impl;

import com.docutools.jocument.MimeType;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Report;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.word.WordReportImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TemplateImpl implements Template {

  private final URL url;
  private final MimeType mimeType;

  public TemplateImpl(URL url, MimeType mimeType) {
    this.url = url;
    this.mimeType = mimeType;
  }

  @Override
  public MimeType getMimeType() {
    return mimeType;
  }

  @Override
  public Report startGeneration(PlaceholderResolver resolver) {
    var report = new WordReportImpl(this, resolver);
    report.start();
    return report;
  }

  @Override
  public InputStream openStream() throws IOException {
    return url.openStream();
  }
}
