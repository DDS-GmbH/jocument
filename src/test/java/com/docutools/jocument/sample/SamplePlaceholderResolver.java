package com.docutools.jocument.sample;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.IterablePlaceholderData;
import com.docutools.jocument.impl.ScalarPlaceholderData;

import java.util.List;
import java.util.Optional;

public class SamplePlaceholderResolver implements PlaceholderResolver {

  private static final List<PlaceholderResolver> services =
          List.of(new ServicePlaceholderResolver("USS Enterprise"),
                  new ServicePlaceholderResolver("US Defiant"));

  @Override
  public Optional<PlaceholderData> resolve(String placeholderName) {
    switch (placeholderName) {
      case "name":
        return Optional.of(new ScalarPlaceholderData("James T. Kirk"));
      case "services":
        return Optional.of(new IterablePlaceholderData(services, services.size()));
      default:
        return Optional.empty();
    }
  }

}
