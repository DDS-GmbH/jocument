package com.docutools.jocument;

import java.util.Optional;

public interface PlaceholderMapper {
  Optional<String> map(String placeholder);

  String tryToMap(String placeholder);
}
