package com.docutools.jocument.image;

/**
 * Can be thrown by an {@link ImageReference} indicating this image was already closed.
 *
 * @author partschi
 * @since 2021-03-24
 */
public class ImageReferenceClosedException extends RuntimeException {

  public ImageReferenceClosedException(String message) {
    super(message);
  }
}
