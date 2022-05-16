package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Optional;

public class WrongParameterSingleParameter {
  @MatchPlaceholder(pattern = "test")
  Optional<String> testMethod(int number){
    return Optional.of("");
  }
}
