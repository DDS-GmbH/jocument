package com.docutools.jocument.impl.template;

import com.docutools.jocument.TemplateSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PathTemplateSource implements TemplateSource {

  private final Path path;

  public PathTemplateSource(Path path) {
    this.path = path;
  }

  public PathTemplateSource(File file) {
    this.path = file.toPath();
  }

  @Override
  public InputStream open() throws IOException {
    return Files.newInputStream(path, StandardOpenOption.READ);
  }
}
