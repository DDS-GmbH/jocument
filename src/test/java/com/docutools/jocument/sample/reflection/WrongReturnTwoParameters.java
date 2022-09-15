package com.docutools.jocument.sample.reflection;

import com.docutools.jocument.annotations.MatchPlaceholder;
import java.util.Locale;

public class WrongReturnTwoParameters {
  @MatchPlaceholder(pattern = "test")
  public String testMethod(String placeholder, Locale locale){
    return "";
  }
}
