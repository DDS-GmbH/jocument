package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Locale;
import java.util.Optional;

public class ValidTwoParameterMatch {
  @MatchPlaceholder(pattern = "test")
  public Optional<String> testMethod(String placeholder, Locale locale){
    return Optional.of("");
  }
}
