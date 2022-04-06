package com.docutools.jocument.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingUtils {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^{}]+)?}}");

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

  public static Matcher matchPlaceholders(String value) {
    return PLACEHOLDER_PATTERN.matcher(value);
  }
}
