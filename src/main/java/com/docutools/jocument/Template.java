package com.docutools.jocument;

import com.docutools.jocument.impl.TemplateImpl;
import com.docutools.jocument.impl.template.InMemoryTemplateSource;
import com.docutools.jocument.impl.template.PathTemplateSource;
import com.docutools.jocument.impl.template.URLTemplateSource;
import org.apache.poi.util.LocaleUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import org.apache.poi.util.LocaleUtil;

/**
 * A template for a given {@link com.docutools.jocument.MimeType}. You can generate
 * {@link Document}s from this template by calling {@link this#startGeneration(PlaceholderResolver)},
 * which runs asynchronously.
 *
 * @author codecitizen
 * @see com.docutools.jocument.MimeType
 * @see com.docutools.jocument.PlaceholderResolver
 * @see Document
 * @since 1.0-SNAPSHOT
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
    return fromClassPath(path, LocaleUtil.getUserLocale());
  }

  /**
   * Creates a {@link com.docutools.jocument.Template} instance from a template file on the classpath.
   *
   * @param path   the resource path
   * @param locale the templates {@link java.util.Locale}
   * @return the {@link com.docutools.jocument.Template} when the resource was found.
   * @throws java.lang.IllegalArgumentException when the files MIME type is not supported.
   */
  static Optional<Template> fromClassPath(String path, Locale locale) {
    var mimeType = MimeType.fromFileExtension(path)
        .orElseThrow(() -> new IllegalArgumentException("Unsupported MIME-Type: " + path));
    return Optional.ofNullable(Template.class.getResource(path))
        .map(url -> new TemplateImpl(new URLTemplateSource(url), mimeType, locale));
  }

  /**
   * Creates a {@link Template} instance from a file.
   *
   * @param path the file path
   * @return the {@link Template} where the file was found
   */
  static Optional<Template> from(Path path) {
    return from(path, LocaleUtil.getUserLocale());
  }

  /**
   * Creates a {@link Template} instance from a file.
   *
   * @param path the file path
   * @param locale of templates {@link Locale}
   * @return the {@link Template} when the file was found
   */
  static Optional<Template> from(Path path, Locale locale) {
    var mimeType = MimeType.fromFileExtension(path.toString())
            .orElseThrow(() -> new IllegalArgumentException("Unsupported MIME-Type: " + path));
    return Optional.of(new TemplateImpl(new PathTemplateSource(path), mimeType, locale));
  }

  /**
   * Creates a {@link Template} instance from a file.
   *
   * @param file the file path
   * @return the {@link Template} when the file was found
   */
  static Optional<Template> from(File file) {
    return from(file.toPath());
  }

  /**
   * Creates a {@link Template} instance from a file.
   *
   * @param file the file path
   * @param locale the templates {@link Locale}
   * @return the {@link Template} when the file was found
   */
  static Optional<Template> from(File file, Locale locale) {
    return from(file.toPath(), locale);
  }

  /**
   * Creates a {@link Template} instance from a URL.
   *
   * @param url the url
   * @return the {@link Template} when the file was found
   */
  static Optional<Template> from(URL url) {
    return from(url, LocaleUtil.getUserLocale());
  }

  /**
   * Creates a {@link Template} instance from a URL.
   *
   * @param url the URL
   * @param locale the templates {@link Locale}
   * @return the {@link Template} when the file was found
   */
  static Optional<Template> from(URL url, Locale locale) {
    var mimeType = MimeType.fromFileExtension(url.getPath())
            .orElseThrow(() -> new IllegalArgumentException("Unsupported MIME-Type: " + url));
    return Optional.of(new TemplateImpl(new URLTemplateSource(url), mimeType, locale));
  }

  /**
   * Creates a {@link Template} from an URI.
   *
   * @param uri the URI
   * @return the {@link Template}
   * @throws MalformedURLException when the uri does not contain an absolute path
   */
  static Optional<Template> from(URI uri) throws MalformedURLException {
    return from(uri.toURL());
  }

  /**
   * Creates an in-memory representation of a {@link Template}.
   *
   * @param data the template file data
   * @param mimeType it's MIME Type
   * @return the in-memory {@link Template}
   */
  static Optional<Template> from(byte[] data, MimeType mimeType) {
    return from(data, mimeType, LocaleUtil.getUserLocale());
  }

  /**
   * Creates an in-memory representation of a {@link Template}.
   *
   * @param data the template file data
   * @param locale the {@link Locale}
   * @param mimeType it's MIME Type
   * @return the in-memory {@link Template}
   */
  static Optional<Template> from(byte[] data, MimeType mimeType, Locale locale) {
    return Optional.of(new TemplateImpl(new InMemoryTemplateSource(data), mimeType, locale));
  }

  /**
   * Creates an in-memory representation of a {@link Template}.
   *
   * @param in the template file data
   * @param mimeType it's MIME Type
   * @return the in-memory {@link Template}
   */
  static Optional<Template> from(InputStream in, MimeType mimeType) throws IOException {
    return from(in, mimeType, LocaleUtil.getUserLocale());
  }

  /**
   * Creates an in-memory representation of a {@link Template}.
   *
   * @param in the template file data
   * @param mimeType it's MIME Type
   * @param locale it's {@link Locale}
   * @return the in-memory {@link Template}
   */
  static Optional<Template> from(InputStream in, MimeType mimeType, Locale locale) throws IOException {
    return Optional.of(new TemplateImpl(new InMemoryTemplateSource(in), mimeType, locale));
  }

  /**
   * The MIME-Type supported by this template.
   *
   * @return {@link java.awt.image.MemoryImageSource}
   */
  MimeType getMimeType();

  /**
   * Gets the {@link java.util.Locale} setting of the template.
   *
   * @return the {@link java.util.Locale}
   */
  Locale getLocale();

  /**
   * Starts the generation of a document for the given {@link com.docutools.jocument.PlaceholderResolver} asynchronously.
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
