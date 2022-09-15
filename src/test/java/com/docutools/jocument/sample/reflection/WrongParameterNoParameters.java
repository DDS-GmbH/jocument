package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Optional;

public class WrongParameterNoParameters {
  @MatchPlaceholder(pattern = "test")
  public Optional<String> testMethod(){
    return Optional.of("");
  }
}
