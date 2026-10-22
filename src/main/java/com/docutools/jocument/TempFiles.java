package com.docutools.jocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates the temporary files jocument writes documents and images to.
 *
 * @since 2026-10-22
 */
public final class TempFiles {

  private static final String PREFIX = "jocument-";

  private TempFiles() {
  }

  /**
   * Creates a temporary file in the given directory.
   *
   * @param directory The directory to create the file in, or {@code null} for the default temporary-file directory
   * @param suffix    The suffix of the file name, e.g. {@code ".docx"}
   * @return The path to the created file
   * @throws IOException If the file could not be created
   */
  public static Path create(Path directory, String suffix) throws IOException {
    return directory == null ? Files.createTempFile(PREFIX, suffix) : Files.createTempFile(directory, PREFIX, suffix);
  }

  /**
   * Deletes a temporary file, adding a failure to delete it as suppressed exception to the given cause.
   *
   * @param file  The file to delete
   * @param cause The exception which made the file obsolete
   */
  public static void deleteQuietly(Path file, Throwable cause) {
    try {
      Files.deleteIfExists(file);
    } catch (IOException e) {
      cause.addSuppressed(e);
    }
  }
}
