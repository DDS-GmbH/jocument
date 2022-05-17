package com.docutools.jocument.image;

import com.docutools.jocument.impl.word.WordImageUtils;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DefaultImageStrategy implements ImageStrategy {

  private static final Logger log = LogManager.getLogger(DefaultImageStrategy.class);

  private static final Object MUTEX = new Object();

  //https://stackoverflow.com/a/7855774/4786733
  private static volatile ImageStrategy INSTANCE;

  /**
   * Gets the singleton instance of the defualt {@link ImageStrategy} prefered by jocument.
   *
   * @return the default {@link ImageStrategy}
   */
  public static ImageStrategy instance() {
    ImageStrategy localRef = INSTANCE;
    if (localRef == null) {
      synchronized (MUTEX) {
        localRef = INSTANCE;
        if (localRef == null) {
          localRef = INSTANCE = new DefaultImageStrategy();
          log.trace("Initialized singleton");
        }
      }
    }
    return localRef;
  }

  private DefaultImageStrategy() {
  }

  @Override
  public ImageReference load(Path path) throws IOException {
    log.trace("Loading image from '{}'", path);
    var image = ImageIO.read(path.toFile());
    return new DefaultImageReference(image);
  }

  @Override
  public ImageReference scale(ImageReference original, double scaleBy) {
    log.trace("Scaling image {} by factor {}", original, scaleBy);

    if (original instanceof DefaultImageReference ref) {
      var source = ref.getImage();
      var target = new BufferedImage((int) (source.getWidth() * scaleBy), (int) (source.getHeight() * scaleBy), source.getType());
      var at = new AffineTransform();
      at.scale(scaleBy, scaleBy);
      var op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
      op.filter(source, target);
      return new DefaultImageReference(target);
    }
    throw new IncompatibleImageReferenceException();
  }

  @Override
  // https://stackoverflow.com/a/12164026/4786733
  public Dimension getDimensions(Path path) throws IOException {
    log.trace("Getting image dimensions of '{}'", path);
    var mimeType = WordImageUtils.probeContentTypeSafely(path).orElseThrow(() -> new IOException("Could not determine File Type"));
    Iterator<ImageReader> iter = ImageIO.getImageReadersByMIMEType(mimeType);
    while (iter.hasNext()) {
      ImageReader reader = iter.next();
      try (ImageInputStream stream = new FileImageInputStream(path.toFile())) {
        reader.setInput(stream);
        int width = reader.getWidth(reader.getMinIndex());
        int height = reader.getHeight(reader.getMinIndex());
        return new Dimension(width, height);
      } catch (IOException e) {
        log.warn("Error reading: %s".formatted(path.toAbsolutePath()), e);
      } finally {
        reader.dispose();
      }
    }
    throw new IOException("Not a known image file: " + path.toAbsolutePath());
  }
}
