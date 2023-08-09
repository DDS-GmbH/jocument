package com.docutools.jocument.sample;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.IterablePlaceholderData;
import com.docutools.jocument.impl.ScalarPlaceholderData;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class SamplePlaceholderResolver extends PlaceholderResolver {

  private static final List<PlaceholderResolver> services =
      List.of(new ServicePlaceholderResolver("USS Enterprise"),
          new ServicePlaceholderResolver("US Defiant"));

  @Override
  protected Optional<PlaceholderData> doResolve(String placeholderName, Locale locale) {
    switch (placeholderName) {
      case "name":
        return Optional.of(new ScalarPlaceholderData<>("James T. Kirk"));
      case "services":
        return Optional.of(new IterablePlaceholderData(services, services.size()));
      default:
        return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return services.stream().map(PlaceholderResolver::toString).collect(Collectors.joining(", "));
  }

}
