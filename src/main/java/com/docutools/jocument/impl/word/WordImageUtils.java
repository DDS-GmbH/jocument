package com.docutools.jocument.impl.word;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.jlibvips.VipsImage;
import org.jlibvips.jna.VipsBindingsSingleton;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;

public class WordImageUtils {

  static {
    VipsBindingsSingleton.configure("/usr/local/Cellar/vips/8.9.1/lib/libvips.42.dylib");
  }

  public static final Map<String, Integer> XWPF_CONTENT_TYPE_MAPPING =
          Map.of(
                  "image/jpeg", XWPFDocument.PICTURE_TYPE_JPEG,
                  "image/jpg", XWPFDocument.PICTURE_TYPE_JPEG,
                  "image/png", XWPFDocument.PICTURE_TYPE_PNG
          );

  public static final int DEFAULT_XWPF_CONTENT_TYPE = XWPFDocument.PICTURE_TYPE_JPEG;

  /**
   * Width is limited by the A4 Page format.
   */
  private static final int MAX_PICTURE_WIDTH = 625;
  /**
   * Width is limited by the A4 Page format.
   */
  private static final int MAX_PICTURE_HEIGHT = 860;

  private static final Dimension DEFAULT_DIM = new Dimension(100, 100);

  public static XWPFPicture insertImage(XWPFParagraph paragraph, Path path) {
    var dim = probeDimensions(path)
            .map(WordImageUtils::scaleToWordSize)
            .map(WordImageUtils::toEMU)
            .orElse(DEFAULT_DIM);
    var contentType = probeImageType(path);

    try(var in = Files.newInputStream(path, StandardOpenOption.READ)) {
      return paragraph.createRun()
              .addPicture(in, contentType, path.getFileName().toString(), dim.width, dim.height);
    } catch (InvalidFormatException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Optional<Dimension> probeDimensions(Path path) {
    VipsImage image = null;
    try {
      image = VipsImage.fromFile(path);
      return Optional.of(new Dimension(image.getWidth(), image.getHeight()));
    } catch(Exception e) {
      return Optional.empty();
    } finally {
      if(image != null)
        image.unref();
    }
  }

  private static Dimension toEMU(Dimension dim) {
    return new Dimension(Units.pixelToEMU(dim.width), Units.pixelToEMU(dim.height));
  }

  private static boolean exceedsMaxWordSize(Dimension dimension) {
    return dimension != null && (dimension.width > MAX_PICTURE_WIDTH || dimension.height > MAX_PICTURE_HEIGHT);
  }

  private static Dimension scaleToWordSize(Dimension dim) {
    if(exceedsMaxWordSize(dim)) {
      return scale(dim, MAX_PICTURE_HEIGHT, MAX_PICTURE_WIDTH);
    }
    return dim;
  }

  public static Dimension scale(Dimension dim, int maxHeight, int maxWidth) {
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
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  private WordImageUtils() {
  }

}
