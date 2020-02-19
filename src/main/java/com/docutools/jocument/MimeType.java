package com.docutools.jocument;

import java.util.Optional;

/**
 * The supported MIME-Types for {@link com.docutools.jocument.Template}s.
 *
 * @since 1.0-SNAPSHOT
 */
public enum MimeType {
  DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

  private final String value;

  MimeType(String value) {
    this.value = value;
  }

  /**
   * Tries to parse the MIME Type from the paths file extension.
   *
   * @param path the path
   * @return the MIME Type
   */
  public static Optional<MimeType> fromFileExtension(String path) {
    if (path == null || path.isBlank()) {
      return Optional.empty();
    }
    var lastDot = path.lastIndexOf('.');
    if (lastDot == -1) {
      return Optional.empty();
    }
    var extension = path.substring(++lastDot).toLowerCase();

    if ("docx".equals(extension)) {
      return Optional.of(DOCX);
    }
    return Optional.empty();
  }

  public String getValue() {
    return value;
  }
}
