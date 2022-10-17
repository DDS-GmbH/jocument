package com.docutools.jocument.sample.model;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record Ship(String name, Captain captain, int crew, List<Service> services, LocalDate built, Optional<String> currentPosition) {

  private static final Logger log = LogManager.getLogger(Ship.class);

  @MatchPlaceholder(pattern = "^built-fmt-.*$")
  public Optional<String> formatBuiltDate(String placeholder) {
    try {
      if (built != null) {
        // strip prefix from placeholder, to receive date format
        var format = placeholder.substring("built-fmt-".length());
        var dtf = DateTimeFormatter.ofPattern(format);
        return Optional.of(dtf.format(built));
      }
    } catch (IllegalArgumentException iae) { // when the given pattern is invalid
      log.error("Could not compile Ship built date pattern from placeholder.", iae);
    }
    return Optional.empty();
  }

  @MatchPlaceholder(pattern = "(?i)numberOfServices")
  public Optional<String> getNumberOfServices(String placeholder) {
    return Optional.of(String.valueOf(services.size()));
  }

  public String shipName() {
    return this.name;
  }
}
