package com.docutools.jocument.sample.placeholders;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import com.docutools.jocument.impl.word.WordUtilities;
import java.util.Locale;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class QuotePlaceholder extends CustomWordPlaceholderData {
  @Override
  protected void transform(IBodyElement placeholder, XWPFDocument document, Locale locale, GenerationOptions options) {
    if (placeholder instanceof XWPFParagraph xwpfParagraph) {
      WordUtilities.replaceText(xwpfParagraph, "Live your life not celebrating victories, but overcoming defeats.");
    }
  }
}
