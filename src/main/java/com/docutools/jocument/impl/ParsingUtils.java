package com.docutools.jocument.impl;

import java.util.List;

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

  public static List<String> getMatchingLoopEnds(String placeholder) {
    return List.of("{{/%s}}".formatted(placeholder), "{{end-%s}}".formatted(placeholder));
  }
}
