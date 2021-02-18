package com.docutools.jocument.impl.word.placeholders;

import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import com.docutools.jocument.impl.word.WordImageUtils;
import com.docutools.jocument.impl.word.WordUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jlibvips.VipsImage;

public class ImagePlaceholderData extends CustomWordPlaceholderData {

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
  protected void transform(IBodyElement placeholder, XWPFDocument document) {
    Path path = applyOptions();
    try {
      var paragraph = document.insertNewParagraph(WordUtilities.openCursor(placeholder).orElseThrow());
      WordImageUtils.insertImage(paragraph, path);
      WordUtilities.removeIfExists(placeholder);
    } finally {
      if (path != null && !path.equals(imagePath)) {
        try {
          Files.deleteIfExists(path);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private Path applyOptions() {
    VipsImage image = null;
    try {
      image = VipsImage.fromFile(imagePath);
      if (maxWidth > 0 && image.getWidth() > maxWidth) {
        double scale = (double) maxWidth / image.getWidth();
        VipsImage resized = image.resize(scale)
            .create();
        image.unref();
        image = resized;
      }
      Path path = image.jpeg()
          .save();
      image.unref();
      return path;
    } catch (Exception e) {
      e.printStackTrace();
      return imagePath;
    } finally {
      if (image != null) {
        image.close();
      }
    }
  }
}
