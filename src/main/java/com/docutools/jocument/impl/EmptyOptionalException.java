package com.docutools.jocument.impl;

public class EmptyOptionalException extends Exception {
  public EmptyOptionalException(String placeholderName) {
    super(placeholderName);
  }
}
