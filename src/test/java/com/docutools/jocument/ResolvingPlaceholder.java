package com.docutools.jocument;

import com.docutools.jocument.sample.SamplePlaceholderResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

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
}
