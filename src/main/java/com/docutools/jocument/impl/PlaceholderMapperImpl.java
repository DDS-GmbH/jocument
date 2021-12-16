package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlaceholderMapperImpl implements PlaceholderMapper {
  private static final Logger logger = LogManager.getLogger();
  private static Map<String, String> placeholderMappings = new HashMap<>();

  static {
    var pathString = System.getenv("DT_JT_RR_PLACEHOLDER_MAPPINGS");
    if (pathString != null) {
      var path = Path.of(pathString);
      var file = path.toFile();
      if (file.exists() && file.canRead()) {
        try {
          var reader = new BufferedReader(new FileReader(file));
          placeholderMappings = reader.lines()
              .filter(line -> !line.isEmpty())
              .filter(line -> !line.startsWith("//"))
              .map(line -> line.split(":"))
              .collect(Collectors.toMap(strings -> strings[0], strings -> strings[1]));
        } catch (IOException e) {
          logger.error(e);
        }
      } else if (!file.exists()) {
        logger.error("Mappings file {} does not exist", pathString);
      } else if (!file.canRead()) {
        logger.error("Mappings file {} can not be read", pathString);
      }
    } else {
      logger.info("No mapper found");
    }
  }

  @Override
  public String map(String placeholder) {
    return placeholderMappings.getOrDefault(placeholder, placeholder);
  }
}
