package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;

public class SinglePlaceholderData implements PlaceholderData {

  private final String value;

  public SinglePlaceholderData(String value) {
    this.value = value;
  }

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.SINGLE;
  }

  @Override
  public String toString() {
    return value;
  }
}
