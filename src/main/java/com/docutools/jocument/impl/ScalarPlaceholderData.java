package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;

public class ScalarPlaceholderData implements PlaceholderData {

  private final String value;

  public ScalarPlaceholderData(String value) {
    this.value = value;
  }

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.SCALAR;
  }

  @Override
  public String toString() {
    return value;
  }
}
