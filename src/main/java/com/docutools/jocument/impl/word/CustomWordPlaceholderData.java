package com.docutools.jocument.impl.word;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.IBodyElement;

public abstract class CustomWordPlaceholderData implements PlaceholderData {
  private static final Logger logger = LogManager.getLogger();

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public void transform(Object placeholder, Locale locale, GenerationOptions options) {
    if (placeholder instanceof IBodyElement element) {
      if (element.getPart() instanceof IBody bodyPart) {
        transform(element, bodyPart, locale, options);
      } else {
        logger.error("Parent of {} is not an instance of IBody", placeholder);
        throw new IllegalArgumentException("Only children of IBody objects accepted.");
      }
    } else {
      logger.error("{} is not an instance of IBodyElement", placeholder);
      throw new IllegalArgumentException("Only IBodyElements accepted.");
    }
  }

  protected abstract void transform(IBodyElement placeholder, IBody part, Locale locale, GenerationOptions options);
}
