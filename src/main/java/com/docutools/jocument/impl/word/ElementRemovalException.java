package com.docutools.jocument.impl.word;

//TODO move to checked expression on major version bump
public class ElementRemovalException extends RuntimeException {
  public ElementRemovalException(Exception e) {
    super(e);
  }
}
