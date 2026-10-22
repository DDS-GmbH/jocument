package com.docutools.jocument.image;

import com.docutools.jocument.TempFiles;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
  public Path saveAsJpeg() throws IOException, NoWriterFoundException {
    return saveAsJpeg(null);
  }

  @Override
  public Path saveAsJpeg(Path directory) throws IOException, NoWriterFoundException {
    return saveAs(directory, ".jpg", "JPEG");
  }

  @Override
  public Path saveAsPng() throws IOException, NoWriterFoundException {
    return saveAsPng(null);
  }

  @Override
  public Path saveAsPng(Path directory) throws IOException, NoWriterFoundException {
    return saveAs(directory, ".png", "PNG");
  }

  private Path saveAs(Path directory, String suffix, String formatName) throws IOException, NoWriterFoundException {
    if (image == null) {
      throw new ImageReferenceClosedException("Image was already closed.");
    }

    var filePath = TempFiles.create(directory, suffix);
    log.trace("Saving image {} to '{}'...", id, filePath);

    try {
      boolean foundWriter = ImageIO.write(image, formatName, filePath.toFile());
      if (!foundWriter) {
        throw new NoWriterFoundException(formatName);
      }
    } catch (IOException | NoWriterFoundException | RuntimeException | Error e) {
      TempFiles.deleteQuietly(filePath, e);
      throw e;
    }
    log.trace("Successfully saved image {} to '{}'", id, filePath);

    return filePath;
  }

  @Override
  public void close() {
    log.trace("Closing image {}", id);
    image = null;
  }
}
