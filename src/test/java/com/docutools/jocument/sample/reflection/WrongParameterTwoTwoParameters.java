package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Locale;
import java.util.Optional;

public class WrongParameterTwoTwoParameters {
  @MatchPlaceholder(pattern = "test")
  public Optional<String> testMethod(String placeholder, int number){
    return Optional.of("");
  }
}
