package com.docutools.jocument;

import java.nio.file.Path;

/**
 * This interface defines the publicly accessible methods of documents,
 * which are created by starting the document generation from a
 * {@link Template}.
 * As one can see, they are mainly responsible for the monitoring of the
 * thread generating the finished document from the template, one can
 * only interact passively with the class.
 *
 * @author codecitizen
 * @see Template
 * @since 2020-02-19
 */
public interface Document {

  /**
   * This method is used to stop execution on the main thread until the
   * generation of the document has finished or a specified time has passed,
   * after which the thread responsible for generation gets interrupted.
   *
   * @param time the time to wait before interrupting the process in milliseconds
   * @throws InterruptedException If document generation has not finished in the
   *                              allowed time.
   */
  void blockUntilCompletion(long time) throws InterruptedException;

  /**
   * Check if the document generation has completed.
   * The `completed` boolean gets set to true if the
   * generation has finished.
   * If the threads get interrupted, it is not set, so
   * this method would return false.
   *
   * @return Whether the document has been generated successfully
   */
  boolean completed();

  /**
   * Get the path to the generated document.
   * This is currently set only after the document generation
   * has finished, but is dependant on the implementation, so this
   * could change in the future.
   *
   * @return The path to the finished document
   */
  Path getPath();
}
