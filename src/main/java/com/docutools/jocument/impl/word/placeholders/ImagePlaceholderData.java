package com.docutools.jocument.impl.word.placeholders;

import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import com.docutools.jocument.impl.word.WordImageUtils;
import com.docutools.jocument.impl.word.WordUtilities;
import java.nio.file.Path;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class ImagePlaceholderData extends CustomWordPlaceholderData {

  private final Path imagePath;

  public ImagePlaceholderData(Path imagePath) {
    this.imagePath = imagePath;
  }

  @Override
  protected void transform(IBodyElement placeholder, XWPFDocument document) {
    var paragraph = document.insertNewParagraph(WordUtilities.openCursor(placeholder).orElseThrow());
    WordImageUtils.insertImage(paragraph, imagePath);
    WordUtilities.removeIfExists(placeholder);
  }
}
