package com.docutools.jocument.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DefaultImageStrategy implements ImageStrategy {

  private static final Logger log = LogManager.getLogger(DefaultImageStrategy.class);

  private static final Object MUTEX = new Object();

  private static ImageStrategy INSTANCE;

  /**
   * Gets the singleton instance of the defualt {@link ImageStrategy} prefered by jocument.
   *
   * @return the default {@link ImageStrategy}
   */
  public static ImageStrategy instance() {
    if (INSTANCE == null) {
      synchronized (MUTEX) {
        if (INSTANCE == null) {
          INSTANCE = new DefaultImageStrategy();
          log.trace("Initialized singleton");
        }
      }
    }
    return INSTANCE;
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
}
