package com.docutools.jocument.impl.word;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.jlibvips.VipsImage;
import org.jlibvips.jna.VipsBindingsSingleton;

public class WordImageUtils {
  public static final Map<String, Integer> XWPF_CONTENT_TYPE_MAPPING =
      Map.of(
          "image/jpeg", Document.PICTURE_TYPE_JPEG,
          "image/jpg", Document.PICTURE_TYPE_JPEG,
          "image/png", Document.PICTURE_TYPE_PNG
      );
  public static final int DEFAULT_XWPF_CONTENT_TYPE = Document.PICTURE_TYPE_JPEG;
  private static final Logger logger = LogManager.getLogger();
  /**
   * Width is limited by the A4 Page format.
   */
  private static final int MAX_PICTURE_WIDTH = 625;
  /**
   * Width is limited by the A4 Page format.
   */
  private static final int MAX_PICTURE_HEIGHT = 860;
  private static final Dimension DEFAULT_DIM = new Dimension(100, 100);

  static {
    VipsBindingsSingleton.configure("/usr/lib64/libvips.so.42");
  }

  private WordImageUtils() {
  }

  /**
   * Inserts the image of the given {@link java.nio.file.Path} into the {@link org.apache.poi.xwpf.usermodel.XWPFParagraph}.
   *
   * @param paragraph the paragraph
   * @param path      the image file
   * @return the inserted image
   */
  public static XWPFPicture insertImage(XWPFParagraph paragraph, Path path) {
    var dim = probeDimensions(path)
        .map(WordImageUtils::scaleToWordSize)
        .map(WordImageUtils::toEmu)
        .orElse(DEFAULT_DIM);
    var contentType = probeImageType(path);

    try (var in = Files.newInputStream(path, StandardOpenOption.READ)) {
      return paragraph.createRun()
          .addPicture(in, contentType, path.getFileName().toString(), dim.width, dim.height);
    } catch (InvalidFormatException | IOException e) {
      logger.error("Could not insert image from given Path %s.".formatted(path), e);
      throw new IllegalArgumentException("Could not insert image form given Path.", e);
    }
  }

  private static Optional<Dimension> probeDimensions(Path path) {
    VipsImage image = null;
    try {
      image = VipsImage.fromFile(path);
      return Optional.of(new Dimension(image.getWidth(), image.getHeight()));
    } catch (UnsatisfiedLinkError e) {
      return fallbackToBufferedImageForDimensionProbe(path);
    } catch (Exception e) {
      logger.warn("Failed to get dimensions of image from path %s".formatted(path), e);
      return Optional.empty();
    } finally {
      if (image != null) {
        image.unref();
      }
    }
  }

  private static Optional<Dimension> fallbackToBufferedImageForDimensionProbe(Path path) {
    try {
      var img = ImageIO.read(path.toFile());
      return Optional.of(new Dimension(img.getWidth(), img.getHeight()));
    } catch (Exception e) {
      logger.error("Could not fallback to BufferedImage for Dimension probe", e);
      return Optional.empty();
    }
  }

  private static Dimension toEmu(Dimension dim) {
    return new Dimension(Units.pixelToEMU(dim.width), Units.pixelToEMU(dim.height));
  }

  private static boolean exceedsMaxWordSize(Dimension dimension) {
    return dimension != null && (dimension.width > MAX_PICTURE_WIDTH || dimension.height > MAX_PICTURE_HEIGHT);
  }

  private static Dimension scaleToWordSize(Dimension dim) {
    if (exceedsMaxWordSize(dim)) {
      return scale(dim, MAX_PICTURE_HEIGHT, MAX_PICTURE_WIDTH);
    }
    return dim;
  }

  private static Dimension scale(Dimension dim, int maxHeight, int maxWidth) {
    //Compares the scale down ratio of the width and height compared to the max size and saves the larger one
    double scale = Math.max((double) dim.height / maxHeight, (double) dim.width / maxWidth);
    int width = (int) (dim.width / scale);
    int height = (int) (dim.height / scale);
    return new Dimension(width, height);
  }

  private static int probeImageType(Path path) {
    return probeContentTypeSafely(path)
        .map(contentType -> XWPF_CONTENT_TYPE_MAPPING.getOrDefault(contentType, DEFAULT_XWPF_CONTENT_TYPE))
        .orElse(DEFAULT_XWPF_CONTENT_TYPE);
  }

  private static Optional<String> probeContentTypeSafely(Path path) {
    try {
      return Optional.ofNullable(Files.probeContentType(path))
          .filter(contentType -> !contentType.isBlank());
    } catch (Exception e) {
      logger.warn("Failed to probe content type of path %s".formatted(path), e);
      return Optional.empty();
    }
  }

}
