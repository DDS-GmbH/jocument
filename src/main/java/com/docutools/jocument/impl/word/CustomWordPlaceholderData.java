package com.docutools.jocument.impl.word;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public abstract class CustomWordPlaceholderData implements PlaceholderData {
  private static final Logger logger = LogManager.getLogger();

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public void transform(Object placeholder, Locale locale, GenerationOptions options) {
    if (!(placeholder instanceof IBodyElement element)) {
      logger.error("{} is not an instance of IBodyElement", placeholder);
      throw new IllegalArgumentException("Only IBodyElements accepted.");
    }

    var document = element.getBody().getXWPFDocument();

    transform(element, document, locale, options);
  }

  protected abstract void transform(IBodyElement placeholder, XWPFDocument document, Locale locale, GenerationOptions options);

}
