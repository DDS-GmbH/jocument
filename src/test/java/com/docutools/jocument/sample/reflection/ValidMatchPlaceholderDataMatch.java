package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import com.docutools.jocument.impl.models.MatchPlaceholderData;
import java.util.Optional;

public class ValidMatchPlaceholderDataMatch {
  public ValidMatchPlaceholderDataMatch() {
  }

  @MatchPlaceholder(pattern = "test")
  public Optional<String> testMethod(MatchPlaceholderData matchPlaceholderData) {
    return Optional.of("");
  }
}
