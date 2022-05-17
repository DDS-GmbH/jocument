package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Optional;

public class ValidSingleParameterMatch {
  public ValidSingleParameterMatch() {
  }

  @MatchPlaceholder(pattern = "test")
  public Optional<String> testMethod(String placeholder) {
    return Optional.of("");
  }
}
