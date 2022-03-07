package com.docutools.jocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.SamplePlaceholderResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.jocument.sample.model.Ship;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Resolving Various Types of Placeholders")
public class ResolvingPlaceholder {

  private final PlaceholderResolver resolver = new SamplePlaceholderResolver();

  @Test
  @DisplayName("Resolve simple textual placeholder.")
  void shouldResolveSimpleTextualPlaceholder() {
    // Act
    PlaceholderData data = resolver.resolve("name")
        .orElseThrow();
    // Assert
    assertThat(data.getType(), is(PlaceholderType.SCALAR));
    assertThat(data.toString(), equalTo("James T. Kirk"));
  }

  @Test
  @DisplayName("Resolve a loop.")
  void shouldResolveALoop() {
    // Act
    PlaceholderData data = resolver.resolve("services")
        .orElseThrow();
    List<String> values = data.stream()
        .map(r -> r.resolve("name"))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(PlaceholderData::toString)
        .collect(Collectors.toList());
    // Assert
    assertThat(data.getType(), is(PlaceholderType.SET));
    assertThat(data.count(), is(2L));
    assertThat(values, contains("USS Enterprise", "US Defiant"));
  }

  @Test
  @DisplayName("Safely return empty when placeholder not defined.")
  void shouldReturnEmptyWhenPlaceholderUndefined() {
    // Act
    var result = resolver.resolve("bridge");
    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  @DisplayName("Resolve the scope upstream.")
  void shouldResolveTheScopeUpstream() {
    // Arrange
    Ship enterprise = SampleModelData.ENTERPRISE;
    var reflectionResolver = new ReflectionResolver(enterprise);
    var officeResolver = reflectionResolver
        .resolve("captain")
        .flatMap(resolved -> resolved.stream().findFirst())
        .flatMap(captain -> captain.resolve("officer"))
        .flatMap(resolved -> resolved.stream().findFirst())
        .orElseThrow();

    // Act
    var shipName = officeResolver.resolve("@.@.name")
        .map(PlaceholderData::toString)
        .orElse("");
    var crew = officeResolver.resolve("crew")
        .map(PlaceholderData::toString)
        .orElse("");
    var captainName = officeResolver.resolve("@.name")
        .map(PlaceholderData::toString)
        .orElse("");
    var officeName = officeResolver.resolve("name")
        .map(PlaceholderData::toString)
        .orElse("");

    // Assert
    assertThat(shipName, equalTo(enterprise.name()));
    assertThat(crew, equalTo(String.valueOf(enterprise.crew())));
    assertThat(captainName, equalTo(enterprise.captain().getName()));
    assertThat(officeName, equalTo(enterprise.captain().getOfficer().getName()));
  }
}
