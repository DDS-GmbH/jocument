package com.docutools.jocument.image;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Represents a reference to a "loaded" image. If the image is loaded into memory or requested on access is open to the
 * implementation of this class and the corresponding {@link ImageStrategy}.
 *
 * @author partschi
 * @see ImageStrategy
 * @since 2021-03-24
 */
public abstract class ImageReference implements AutoCloseable {

  protected final UUID id;
  protected final int width;
  protected final int height;

  /**
   * Property constructor.
   *
   * @param width width in pixel
   * @param height height in pixel
   */
  protected ImageReference(int width, int height) {
    this.id = UUID.randomUUID();
    this.width = width;
    this.height = height;
  }

  /**
   * A unique ID representing a load of this image.
   *
   * @return the ID
   */
  public UUID getId() {
    return id;
  }

  /**
   * Gets the width of this image in pixel.
   *
   * @return width in pixel
   */
  public int getWidth() {
    return width;
  }

  /**
   * Gets the height of this image in pixel.
   *
   * @return height in pixel
   */
  public int getHeight() {
    return height;
  }

  /**
   * Saves the image to a temporary file directory as JPEG. Won't affect original file.
   *
   * @return the {@link Path} to the JPEG
   */
  public abstract Path saveAsJpeg() throws IOException, NoWriterFoundException;

  public abstract Path saveAsPng() throws IOException, NoWriterFoundException;

  @Override
  public abstract void close();

  @Override
  public String toString() {
    return "ImageReference[%s, %dx%d]".formatted(id, width, height);
  }
}
