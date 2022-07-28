package com.docutools.jocument.impl.word;

import com.docutools.jocument.image.ImageStrategy;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;

public class WordImageUtils {
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

  private WordImageUtils() {
  }

  /**
   * Inserts the image of the given {@link java.nio.file.Path} into the {@link org.apache.poi.xwpf.usermodel.XWPFParagraph}.
   *
   * @param paragraph     the paragraph
   * @param path          the image file
   * @param imageStrategy the {@link ImageStrategy}
   * @return the inserted image
   */
  public static XWPFPicture insertImage(XWPFParagraph paragraph, Path path, ImageStrategy imageStrategy) {
    return insertImage(paragraph, path, imageStrategy, new Dimension(MAX_PICTURE_WIDTH, MAX_PICTURE_HEIGHT));
  }

  /**
   * Inserts the image of the given {@link java.nio.file.Path} into the {@link org.apache.poi.xwpf.usermodel.XWPFParagraph}.
   *
   * @param paragraph        the paragraph
   * @param path             the image file
   * @param imageStrategy    the {@link ImageStrategy}
   * @param targetDimensions the target dimensions to scale the image to
   * @return the inserted image
   */
  public static XWPFPicture insertImage(XWPFParagraph paragraph, Path path, ImageStrategy imageStrategy, Dimension targetDimensions) {
    var dim = probeDimensions(path, imageStrategy)
        .map(pictureDimensions -> scaleToTargetDimensions(pictureDimensions, targetDimensions))
        .map(WordImageUtils::toEmu)
        .orElse(DEFAULT_DIM);
    var contentType = probeImageType(path, imageStrategy);

    try (var in = Files.newInputStream(path, StandardOpenOption.READ)) {
      return paragraph.createRun()
          .addPicture(in, contentType, path.getFileName().toString(), dim.width, dim.height);
    } catch (InvalidFormatException | IOException e) {
      logger.error("Could not insert image from given Path %s.".formatted(path), e);
      throw new IllegalArgumentException("Could not insert image form given Path.", e);
    }
  }


  /**
   * Get the dimensions of the image at the provided path, if possible.
   *
   * @param path          The path to the image to get the dimensions from
   * @param imageStrategy The image strategy to use for figuring out the dimensions
   * @return An {@link Optional} containing the dimensions if they could be determined successfully, {@link Optional#empty()} if not
   */
  public static Optional<Dimension> probeDimensions(Path path, ImageStrategy imageStrategy) {
    try {
      return Optional.of(imageStrategy.getDimensions(path));
    } catch (Exception any) {
      logger.error("Could not probe image '%s' for dimensions.".formatted(path), any);
      return Optional.empty();
    }
  }

  private static Dimension toEmu(Dimension dim) {
    return new Dimension(Units.pixelToEMU(dim.width), Units.pixelToEMU(dim.height));
  }

  private static boolean exceedsTargetDimensions(Dimension imageDimensions, Dimension targetDimensions) {
    return imageDimensions != null && (imageDimensions.width > targetDimensions.width || imageDimensions.height > targetDimensions.height);
  }

  private static Dimension scaleToTargetDimensions(Dimension pictureDimensions, Dimension targetDimension) {
    if (exceedsTargetDimensions(pictureDimensions, targetDimension)) {
      return scale(pictureDimensions, targetDimension.height, targetDimension.width);
    }
    return pictureDimensions;
  }

  private static Dimension scale(Dimension dim, int maxHeight, int maxWidth) {
    //Compares the scale down ratio of the width and height compared to the max size and saves the larger one
    double scale = Math.max(dim.getHeight() / maxHeight, dim.getWidth() / maxWidth);
    int width = (int) Math.ceil(dim.getWidth() / scale);
    int height = (int) Math.ceil(dim.getHeight() / scale);
    return new Dimension(width, height);
  }

  /**
   * Get the XWPF integer representing the image type of the file at the provided {@link Path}, returning `DEFAULT_XWPF_CONTENT_TYPE` if it can not be
   * determined.
   *
   * @param path          the path to the image file
   * @param imageStrategy the image strategy containing a file type resolving function
   * @return the {@link int} representing the image type in the xwpf system.
   */
  public static int probeImageType(Path path, ImageStrategy imageStrategy) {
    try {
      String mimeType = imageStrategy.getMimeType(path);
      return WordImageUtils.toPoiType(mimeType);
    } catch (IOException e) {
      return DEFAULT_XWPF_CONTENT_TYPE;
    }
  }

  private static int toPoiType(String mimeType) {
    return switch (mimeType) {
      case "image/x-emf" -> Document.PICTURE_TYPE_EMF;
      case "image/x-wmf" -> Document.PICTURE_TYPE_WMF;
      case "image/pict" -> Document.PICTURE_TYPE_PICT;
      case "image/jpeg", "image/jpg" -> Document.PICTURE_TYPE_JPEG;
      case "image/png" -> Document.PICTURE_TYPE_PNG;
      case "image/dib" -> Document.PICTURE_TYPE_DIB;
      case "image/gif" -> Document.PICTURE_TYPE_GIF;
      case "image/tiff" -> Document.PICTURE_TYPE_TIFF;
      case "application/postscript" -> Document.PICTURE_TYPE_EPS;
      case "image/bmp" -> Document.PICTURE_TYPE_BMP;
      case "image/wpg" -> Document.PICTURE_TYPE_WPG;
      default -> Document.PICTURE_TYPE_JPEG;
    };
  }
}
