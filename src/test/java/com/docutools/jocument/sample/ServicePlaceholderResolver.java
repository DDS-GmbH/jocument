package com.docutools.jocument.sample;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.ScalarPlaceholderData;
import java.util.Locale;
import java.util.Optional;

public class ServicePlaceholderResolver extends PlaceholderResolver {

  private final String name;

  public ServicePlaceholderResolver(String name) {
    this.name = name;
  }

  @Override
  protected Optional<PlaceholderData> doResolve(String placeholderName, Locale locale) {
    switch (placeholderName) {
      case "name":
        return Optional.of(new ScalarPlaceholderData<>(name));
      default:
        return Optional.empty();
    }
  }

}
