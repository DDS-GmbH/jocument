package com.docutools.jocument.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DefaultImageReference extends ImageReference {

  private static final Logger log = LogManager.getLogger(DefaultImageReference.class);

  private BufferedImage image;

  /**
   * Creates new {@link DefaultImageReference} from {@link BufferedImage}.
   *
   * @param image the image
   */
  public DefaultImageReference(BufferedImage image) {
    super(image.getWidth(), image.getHeight());
    this.image = image;
    log.trace("Initialized new image {}", id);
  }

  /**
   * Returns the {@link BufferedImage} associated with this instance.
   *
   * @return the {@link BufferedImage}
   * @throws ImageReferenceClosedException if {@link AutoCloseable#close()} was already called on this instance.
   */
  public BufferedImage getImage() {
    if (image == null) {
      throw new ImageReferenceClosedException("Image was already closed.");
    }

    return image;
  }

  @Override
  public Path saveAsJpeg() throws IOException {
    if (image == null) {
      throw new ImageReferenceClosedException("Image was already closed.");
    }

    var filePath = Files.createTempFile("jocument", "jpg");
    log.trace("Saving image {} to '{}'...", id, filePath);

    ImageIO.write(image, "JPEG", filePath.toFile());
    log.trace("Successfully saved image {} to '{}'!", id, filePath);

    return filePath;
  }

  @Override
  public void close() {
    log.trace("Closing image {}", id);
    image = null;
  }
}
