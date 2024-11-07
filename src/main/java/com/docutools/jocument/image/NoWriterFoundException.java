package com.docutools.jocument.image;

public class NoWriterFoundException extends Exception {
  public NoWriterFoundException(String type) {
    super("Found no writer for type %s".formatted(type));
  }
}
