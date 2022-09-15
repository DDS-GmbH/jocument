package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Locale;
import java.util.Optional;

public class WrongParameterOneTwoParameters {
  @MatchPlaceholder(pattern = "test")
  public Optional<String> testMethod(int number, Locale locale){
    return Optional.of("");
  }
}
