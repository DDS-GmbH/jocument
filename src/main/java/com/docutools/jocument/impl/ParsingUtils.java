package com.docutools.jocument.impl;

public class ParsingUtils {

  private ParsingUtils() {
  }

  /**
   * Removes enclosing double brackets from the given string.
   *
   * @param value the string
   * @return the unenclosed string
   */
  public static String stripBrackets(String value) {
    if (value.length() < 4) {
      return value;
    }
    return value.substring(2, value.length() - 2);
  }

  public static String getMatchingLoopEnd(String placeholder) {
    return String.format("{{/%s}}", placeholder);
  }

}
