package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlaceholderMapperImpl implements PlaceholderMapper {
  private static final Logger logger = LogManager.getLogger();
  private static Map<String, String> placeholderMappings;
  private static String pathString = System.getenv("DT_JT_RR_PLACEHOLDER_MAPPINGS");

  public static void configure(String pathString) {
    PlaceholderMapperImpl.pathString = pathString;
  }

  @Override
  public Optional<String> map(String placeholder) {
    if (placeholderMappings == null) {
      setup();
    }
    if (placeholderMappings == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(placeholderMappings.get(placeholder.toLowerCase()));
  }

  @Override
  public String tryToMap(String placeholder) {
    return map(placeholder).orElse(placeholder);
  }

  private static void setup() {
    if (pathString != null) {
      var path = Path.of(pathString);
      var file = path.toFile();
      if (file.exists() && file.canRead()) {
        try (var reader = new BufferedReader(new FileReader(file))) {
          logger.debug("Parsing mappings");
          placeholderMappings = reader.lines()
              .filter(line -> !line.isEmpty())
              .filter(line -> !line.startsWith("//"))
              .map(line -> line.split(":"))
              .collect(Collectors.toMap(strings -> strings[0].toLowerCase(), strings -> strings[1]));
          logger.debug("Parsed mappings");
        } catch (IOException e) {
          logger.error(e);
        }
      } else if (!file.exists()) {
        logger.error("Mappings file {} does not exist", pathString);
      } else if (!file.canRead()) {
        logger.error("Mappings file {} can not be read", pathString);
      }
    } else {
      logger.debug("Path to mapping file is null");
    }
  }
}
