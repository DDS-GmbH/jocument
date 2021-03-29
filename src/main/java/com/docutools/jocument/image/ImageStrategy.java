package com.docutools.jocument.image;

import java.io.IOException;
import java.nio.file.Path;

/**
 * jocument allows the insertion and copying of images in it's templates. Therefore it sometimes needs to probe for
 * image metadata or transform images (e.g. resizing). These operations can have a big impact on the performance of
 * template generation, therefore we should allow different strategies on how image processing is done.
 *
 * <p>By default we'll provide a version that works with most common image types and does not require any additional
 * dependencies to jocument.</p>
 *
 * @author partschi
 * @since 2021-03-24
 */
public interface ImageStrategy {

  /**
   * Loads an image from the given {@link Path}. This will allow the caller to probe metadata (e.g. resolution) of
   * the image or use it in subsequent operations like {@link this#scale(ImageReference, double)}.
   *
   * <p>This method potentially loads the whole image into memory, be aware of it's consumption.</p>
   *
   * <p>The caller is responsible for calling {@link ImageReference#close()}.</p>
   *
   * @param path the path to the image file
   * @return the reference to the in-memory image
   */
  ImageReference load(Path path) throws IOException;

  /**
   * Scales an image by the given factor (can be > or < to 1.0) into a new in-memory image and returns it. The OG
   * image remains untouched.
   *
   * <p>The caller is responsible for calling {@link ImageReference#close()} on the new image.</p>
   *
   * @param original the original in-memory image
   * @param scaleBy  the scaling factor (>= 0)
   * @return the scaled image
   */
  ImageReference scale(ImageReference original, double scaleBy);

}
