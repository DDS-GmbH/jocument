package com.docutools.jocument.impl.word.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import com.docutools.jocument.impl.word.WordImageUtils;
import com.docutools.jocument.impl.word.WordUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class ImagePlaceholderData extends CustomWordPlaceholderData {
  private static final Logger logger = LogManager.getLogger();

  private final Path imagePath;

  // Options
  private int maxWidth;

  public ImagePlaceholderData(Path imagePath) {
    this.imagePath = imagePath;
  }

  public ImagePlaceholderData withMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
    return this;
  }

  @Override
  protected void transform(IBodyElement placeholder, XWPFDocument document, Locale locale, GenerationOptions options) {
    Path path = applyOptions(options);
    try {
      var paragraph = document.insertNewParagraph(WordUtilities.openCursor(placeholder).orElseThrow());
      WordImageUtils.insertImage(paragraph, path, options.imageStrategy());
      WordUtilities.removeIfExists(placeholder);
    } finally {
      if (path != null && !path.equals(imagePath)) {
        try {
          Files.deleteIfExists(path);
        } catch (IOException e) {
          logger.error(e);
        }
      }
    }
  }

  private Path applyOptions(GenerationOptions options) {
    try {
      var image = options.imageStrategy().load(imagePath);
      if (maxWidth > 0 && image.getWidth() > maxWidth) {
        double scale = (double) maxWidth / image.getWidth();
        var resized = options.imageStrategy().scale(image, scale);
        image.close();
        image = resized;
      }
      Path path = image.saveAsJpeg();
      image.close();
      return path;
    } catch (Exception e) {
      logger.error(e);
      return imagePath;
    }
  }
}
