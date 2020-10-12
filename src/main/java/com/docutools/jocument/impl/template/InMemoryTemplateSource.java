package com.docutools.jocument.impl.template;

import com.docutools.jocument.TemplateSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InMemoryTemplateSource implements TemplateSource {

  private final byte[] data;

  public InMemoryTemplateSource(byte[] data) {
    this.data = data;
  }

  /**
   * Creates a new in-memory template source.
   *
   * @param in the input stream of the template
   * @throws IOException if there is a problem with transferring data from the input stream to the byte array
   */
  public InMemoryTemplateSource(InputStream in) throws IOException {
    try (var bos = new ByteArrayOutputStream()) {
      in.transferTo(bos);
      this.data = bos.toByteArray();
    }
  }

  @Override
  public InputStream open() throws IOException {
    return new ByteArrayInputStream(data);
  }
}
