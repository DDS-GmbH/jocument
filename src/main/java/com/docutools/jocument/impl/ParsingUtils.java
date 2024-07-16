package com.docutools.jocument.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParsingUtils {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^{}]+)?}}");
  private static final Logger logger = LogManager.getLogger();

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
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      logger.debug("String {} did not contain a placeholder", value);
      return value;
    }
  }

  public static List<String> getMatchingLoopEnds(String placeholder) {
    return List.of("{{/%s}}".formatted(placeholder).toLowerCase(), "{{end-%s}}".formatted(placeholder).toLowerCase());
  }

  public static Matcher matchPlaceholders(String value) {
    return PLACEHOLDER_PATTERN.matcher(value);
  }
}
