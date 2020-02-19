package com.docutools.jocument.impl.word;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public abstract class CustomWordPlaceholderData implements PlaceholderData {

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.CUSTOM;
  }

  @Override
  public void transform(Object placeholder) {
    if (!(placeholder instanceof IBodyElement)) {
      throw new IllegalArgumentException("Only IBodyElements accepted.");
    }

    var element = (IBodyElement) placeholder;
    var document = element.getBody().getXWPFDocument();

    transform(element, document);
  }

  protected abstract void transform(IBodyElement placeholder, XWPFDocument document);

}
