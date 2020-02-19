package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Report;
import com.docutools.jocument.Template;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public abstract class ReportImpl extends Thread implements Report {

  public static final Pattern TAG_PATTERN = Pattern.compile("\\{\\{([A-Za-z0-9[äÄöÖüÜßẞ]-/#]+?)}}");

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
      throw new RuntimeException(e);
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
