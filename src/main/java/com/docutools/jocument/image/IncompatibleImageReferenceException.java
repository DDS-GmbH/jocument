package com.docutools.jocument.image;

/**
 * Thrown by {@link ImageStrategy} if the given {@link ImageReference} implementation is incompatible with it.
 *
 * @since 2021-03-23
 * @author partschi
 */
public class IncompatibleImageReferenceException extends Exception {
  public IncompatibleImageReferenceException(String message) {
    super(message);
  }
}
