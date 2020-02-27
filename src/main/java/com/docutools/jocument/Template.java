package com.docutools.jocument;

import com.docutools.jocument.impl.TemplateImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * A template for a given {@link com.docutools.jocument.MimeType}. You can generate
 * {@link Document}s from this template by calling {@link this#startGeneration(PlaceholderResolver)},
 * which runs asynchronously.
 *
 * @author codecitizen
 * @since 1.0-SNAPSHOT
 * @see com.docutools.jocument.MimeType
 * @see com.docutools.jocument.PlaceholderResolver
 * @see Document
 */
public interface Template {

  /**
   * Creates a {@link com.docutools.jocument.Template} instance from a template file on the classpath.
   *
   * @param path the resource path
   * @return the {@link com.docutools.jocument.Template} when the resource was found.
   * @throws java.lang.IllegalArgumentException when the files MIME type is not supported.
   */
  static Optional<Template> fromClassPath(String path) {
    var mimeType = MimeType.fromFileExtension(path)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported MIME-Type: " + path));
    return Optional.ofNullable(Template.class.getResource(path))
            .map(url -> new TemplateImpl(url, mimeType));
  }

  /**
   * The MIME-Type supported by this template.
   *
   * @return {@link java.awt.image.MemoryImageSource}
   */
  MimeType getMimeType();

  /**
   * Starts the generation of a report for the given {@link com.docutools.jocument.PlaceholderResolver} asynchronously.
   *
   * @param resolver the {@link com.docutools.jocument.PlaceholderResolver}
   * @return the {@link Document}
   */
  Document startGeneration(PlaceholderResolver resolver);

  /**
   * Opens a {@link java.io.InputStream} to the template file. Intended for internal use.
   *
   * @return {@link java.io.InputStream}, caller has to close.
   * @throws IOException when the stream couldn't be opened.
   */
  InputStream openStream() throws IOException;
}
