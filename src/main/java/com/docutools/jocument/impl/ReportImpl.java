package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Report;
import com.docutools.jocument.Template;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public abstract class ReportImpl extends Thread implements Report {

  public static final Pattern TAG_PATTERN = Pattern.compile("\\{\\{([A-Za-z0-9[\u00c4\u00e4\u00d6\u00f6\u00dc\u00fc\u00df]-/#]+?)}}");

  protected final Template template;
  protected final PlaceholderResolver resolver;

  private boolean complete = false;
  private Path path;

  public ReportImpl(Template template, PlaceholderResolver resolver) {
    this.template = template;
    this.resolver = resolver;
  }

  protected abstract Path generate() throws IOException;

  @Override
  public void run() {
    try {
      this.path = generate();
    } catch (IOException e) {
      throw new IllegalStateException("Got IOException when generating, probably due to template.", e);
    }
    complete = true;
  }

  @Override
  public void blockUntilCompletion(long millis) throws InterruptedException {
    super.join(millis);
  }

  @Override
  public boolean completed() {
    return complete;
  }

  @Override
  public Path getPath() {
    return path;
  }
}
