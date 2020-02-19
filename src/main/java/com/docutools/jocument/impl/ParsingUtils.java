package com.docutools.jocument.impl;

public class ParsingUtils {

  public static String stripBrackets(String value) {
    if(value.length() < 4)
      return value;
    return value.substring(2, value.length()-2);
  }

  private ParsingUtils() {
  }

}
