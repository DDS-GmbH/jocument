package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Optional;

public class WrongReturnSingleParameter {
  @MatchPlaceholder(pattern = "test")
  public String testMethod(String placeholder){
    return "";
  }
}
