package com.docutools.jocument.impl.word;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;
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
  public void transform(Object placeholder, GenerationOptions options) {
    if (!(placeholder instanceof IBodyElement)) {
      logger.error("{} is not an instance of IBodyElement", placeholder);
      throw new IllegalArgumentException("Only IBodyElements accepted.");
    }

    var element = (IBodyElement) placeholder;
    var document = element.getBody().getXWPFDocument();

    transform(element, document, options);
  }

  protected abstract void transform(IBodyElement placeholder, XWPFDocument document, GenerationOptions options);

}
