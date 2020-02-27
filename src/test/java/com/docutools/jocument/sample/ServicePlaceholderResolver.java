package com.docutools.jocument.sample;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.ScalarPlaceholderData;

import java.util.Optional;

public class ServicePlaceholderResolver implements PlaceholderResolver {

  private final String name;

  public ServicePlaceholderResolver(String name) {
    this.name = name;
  }

  @Override
  public Optional<PlaceholderData> resolve(String placeholderName) {
    switch (placeholderName) {
      case "name":
        return Optional.of(new ScalarPlaceholderData(name));
      default:
        return Optional.empty();
    }
  }

}
