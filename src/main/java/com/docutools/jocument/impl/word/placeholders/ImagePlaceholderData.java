package com.docutools.jocument.impl.word.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.image.ImageReference;
import com.docutools.jocument.image.IncompatibleImageReferenceException;
import com.docutools.jocument.image.NoWriterFoundException;
import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import com.docutools.jocument.impl.word.WordImageUtils;
import com.docutools.jocument.impl.word.WordUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class ImagePlaceholderData extends CustomWordPlaceholderData {
  private static final Logger logger = LogManager.getLogger();

  private final Path imagePath;

  // Options
  private int maxWidth = -1;
  private int maxHeight = -1;
  private boolean deleteAfterInsertion;

  public ImagePlaceholderData(Path imagePath) {
    this.imagePath = imagePath;
  }

  /**
   * Set maximum width of the image. A width of 0 is not permitted and will be ignored.
   *
   * @param maxWidth The maximum width the image should have.
   * @return the image placeholder data with the maxWidth applied (if != 0)
   */
  public ImagePlaceholderData withMaxWidth(int maxWidth) {
    if (maxWidth == 0) {
      logger.warn("A max width of 0 is not permitted");
      return this;
    }
    this.maxWidth = maxWidth;
    return this;
  }

  /**
   * Set max height of the image. A height of 0 is not permitted and will be ignored.
   *
   * @param maxHeight The maximum height the image should have.
   * @return the image placeholder data with the maxHeight applied (if != 0)
   */
  public ImagePlaceholderData withMaxHeight(int maxHeight) {
    if (maxHeight == 0) {
      logger.warn("A max height of 0 is not permitted");
      return this;
    }
    this.maxHeight = maxHeight;
    return this;
  }

  public ImagePlaceholderData withFileDeletionAfterInsertion(boolean deleteAfterInsertion) {
    this.deleteAfterInsertion = deleteAfterInsertion;
    return this;
  }

  @Override
  protected void transform(IBodyElement placeholder, IBody part, Locale locale, GenerationOptions options) {
    Path path = applyOptions(options);
    var paragraph = part.insertNewParagraph(WordUtilities.openCursor(placeholder).orElseThrow());
    if (placeholder instanceof XWPFParagraph placeholderParagraph) {
      paragraph.setAlignment(placeholderParagraph.getAlignment());
    }
    try {
      WordImageUtils.insertImage(paragraph, path, options.imageStrategy());
    } catch (Exception e) {
      logger.error("Could not insert image", e);
      WordUtilities.replaceText(paragraph, "-");
    } finally {
      WordUtilities.removeIfExists(placeholder);

      if (path != null && !path.equals(imagePath)) {
        try {
          Files.deleteIfExists(path);
        } catch (IOException e) {
          logger.warn(e);
        }
      }
      if (deleteAfterInsertion) {
        try {
          Files.deleteIfExists(imagePath);
        } catch (IOException e) {
          logger.warn(e);
        }
      }
    }
  }

  private Path applyOptions(GenerationOptions options) {
    try (var image = options.imageStrategy().load(imagePath)) {
      double scale = Math.max(image.getWidth() / (double) maxWidth, image.getHeight() / (double) maxHeight);
      if (scale > 1.0) {
        try (var resized = options.imageStrategy().scale(image, 1 / scale)) {
          return saveImage(resized);
        }
      }
      return saveImage(image);
    } catch (IOException | NoWriterFoundException | IncompatibleImageReferenceException e) {
      logger.error(e);
      return imagePath;
    }
  }

  private Path saveImage(ImageReference imageReference) throws IOException, NoWriterFoundException {
    try {
      return imageReference.saveAsJpeg();
    } catch (NoWriterFoundException e) {
      try {
        return imageReference.saveAsPng();
      } catch (NoWriterFoundException ex) {
        throw new NoWriterFoundException("JPG,PNG");
      }
    }
  }
}
