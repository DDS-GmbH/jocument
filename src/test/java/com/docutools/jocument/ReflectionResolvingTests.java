package com.docutools.jocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.docutools.jocument.impl.CustomPlaceholderRegistryImpl;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.Person;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.jocument.sample.model.Ship;
import com.docutools.jocument.sample.model.Uniform;
import com.docutools.jocument.sample.reflection.ValidMatchPlaceholderDataMatch;
import com.docutools.jocument.sample.reflection.ValidSingleParameterMatch;
import com.docutools.jocument.sample.reflection.ValidTwoParameterMatch;
import com.docutools.jocument.sample.reflection.WrongParameterNoParameters;
import com.docutools.jocument.sample.reflection.WrongParameterOneTwoParameters;
import com.docutools.jocument.sample.reflection.WrongParameterSingleParameter;
import com.docutools.jocument.sample.reflection.WrongParameterTwoTwoParameters;
import com.docutools.jocument.sample.reflection.WrongReturnSingleParameter;
import com.docutools.jocument.sample.reflection.WrongReturnTwoParameters;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Resolve placeholders from an object graph via reflection.")
@Tag("automated")
class ReflectionResolvingTests {

  private PlaceholderResolver resolver;

  @BeforeEach
  void setup() {
    resolver = new ReflectionResolver(SampleModelData.PICARD);
  }

  @Test
  @DisplayName("Resolve attributes.")
  void shouldResolveAttributes() {
    // Act
    String name = resolver.resolve("name")
        .map(PlaceholderData::toString)
        .orElseThrow();
    int rank = resolver.resolve("rank")
        .map(PlaceholderData::toString)
        .map(Integer::parseInt)
        .orElseThrow();
    Uniform uniform = resolver.resolve("uniform")
        .map(PlaceholderData::toString)
        .map(Uniform::valueOf)
        .orElseThrow();
    // Assert
    assertThat(name, equalTo(SampleModelData.PICARD.getName()));
    assertThat(rank, is(4));
    assertThat(uniform, is(Uniform.Red));
  }

  @Test
  @DisplayName("Should resolve embedded object.")
  void shouldResolveObject() {
    // Act
    PlaceholderResolver embeddedResolver = resolver.resolve("officer")
        .flatMap(r -> r.stream().findFirst())
        .orElseThrow();
    String name = embeddedResolver.resolve("name")
        .map(PlaceholderData::toString)
        .orElseThrow();
    // Act
    assertThat(name, equalTo("Riker"));
  }

  @Test
  @DisplayName("Return empty when placeholder is not defined.")
  void shouldReturnEmptyWhenPlaceholderUndefined() {
    // Act
    var data = resolver.resolve("crew");
    // Assert
    assertThat(data.isEmpty(), is(true));
  }

  @Test
  @DisplayName("Resolve collection.")
  void shouldResolveCollection() {
    // Act
    List<String> shipNames = resolver.resolve("services")
        .map(data -> data.stream()
            .map(r -> r.resolve("shipName")
                .map(PlaceholderData::toString)
                .orElseThrow())
            .collect(Collectors.toList()))
        .orElseThrow();
    // Act
    assertThat(shipNames, contains("USS Enterprise", "US Defiant"));
  }

  @Test
  @DisplayName("Resolve transitively")
  void shouldResolveTransitively() {
    // Act
    var officerName = resolver.resolve("officer.name")
            .map(Object::toString)
            .orElse("");

    // Assert
    assertThat(officerName, equalTo("Riker"));
  }

  @Test
  @DisplayName("Resolve self")
  void shouldResolveSelf() {
    // Act
    var captainsName = resolver.resolve("this")
            .flatMap(self -> self.stream().findFirst())
            .flatMap(self -> self.resolve("name"))
            .map(Objects::toString)
            .orElse("");

    // Assert
    assertThat(captainsName, equalTo(SampleModelData.PICARD.getName()));
  }

  @Test
  @DisplayName("Resolve record")
  void shouldResolveRecord() {
    // Assemble
    resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);

    // Act
    var shipName = resolver.resolve("name")
        .map(Object::toString)
        .orElseThrow();
    var captain = resolver.resolve("captain.name")
        .map(Object::toString)
        .orElseThrow();
    var shipCrew = resolver.resolve("crew")
        .map(Object::toString)
        .map(Integer::parseInt)
        .orElseThrow();
    var visitedPlanets = resolver.resolve("services")
        .orElseThrow()
        .stream()
        .flatMap(placeholderResolver -> placeholderResolver.resolve("visitedPlanets")
            .orElseThrow()
            .stream())
        .map(placeholderResolver -> placeholderResolver.resolve("planetName")
            .orElseThrow())
        .map(Object::toString)
        .collect(Collectors.toList());

    // Assert
    assertThat(shipName, equalTo(SampleModelData.ENTERPRISE.name()));
    assertThat(captain, equalTo(SampleModelData.PICARD.getName()));
    assertThat(shipCrew, equalTo(5));
    assertThat(visitedPlanets, contains("Mars", "Venus", "Jupiter"));
  }

  @Test
  @DisplayName("Resolve by Regex")
  void shouldResolveByRegex() {
    // Assemble
    var rawPattern = "dd-MM-yyyy";
    var pattern = DateTimeFormatter.ofPattern(rawPattern);
    resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);

    // Act
    var  builtDate = resolver.resolve("built-fmt-" + rawPattern)
        .map(Object::toString)
            .orElseThrow();

    // Assert
    assertThat(builtDate, equalTo(pattern.format(SampleModelData.ENTERPRISE.built())));
  }

  @Test
  @DisplayName("Resolve by Regex ignoring case")
  void shouldResolveByIgnoreCaseRegex() {
    // Assemble
    resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);

    // Act
    var  numberOfServices = resolver.resolve("numberofservices")
        .map(Object::toString)
        .orElseThrow();

    // Assert
    assertThat(numberOfServices, equalTo(String.valueOf(SampleModelData.ENTERPRISE.services().size())));
  }

  @ValueSource(strings = {"name", "captain", "crew", "services", "currentPosition"})
  @ParameterizedTest(name = "Resolve falsy condition for {0} on empty ship")
  void shouldResolveFalsyCondition(String propertyName) {
    // Assemble
    var emptyShip = new Ship("", null, 0, List.of(), LocalDate.now(), Optional.empty());
    var resolver = new ReflectionResolver(emptyShip);

    // Act
    var emptyPlaceholderData = resolver.resolve(propertyName + "?")
        .orElseThrow();

    // Assert
    assertThat(emptyPlaceholderData.isTruthy(), is(false));
  }

  @Test
  void shouldResolveNonemptyOptional() {
    // Assemble
    var resolver = new ReflectionResolver(SampleModelData.ENTERPRISE);

    // Act
    var position = resolver.resolve("currentPosition")
        .orElseThrow();

    // Assert
    assertThat(position.getRawValue(), equalTo(SampleModelData.ENTERPRISE.currentPosition().get()));
  }

  @ParameterizedTest(name = "Resolve falsy condition for {0} on empty ship")
  @ValueSource(classes = {ValidSingleParameterMatch.class, ValidTwoParameterMatch.class, ValidMatchPlaceholderDataMatch.class})
  void shouldResolveValidMatchMethods(Object clazz)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    // Assemble
    var resolver = new ReflectionResolver(((Class<?>) clazz).getConstructor().newInstance());

    // Act
    var result = resolver.resolve("test");

    // Assert
    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getType(), equalTo(PlaceholderType.SCALAR));
    assertThat(result.get().getRawValue(), equalTo(""));
  }

  @ParameterizedTest(name = "Resolve falsy condition for {0} on empty ship")
  @ValueSource(classes = {WrongParameterSingleParameter.class, WrongReturnSingleParameter.class, WrongParameterOneTwoParameters.class,
      WrongParameterTwoTwoParameters.class, WrongReturnTwoParameters.class, WrongParameterNoParameters.class})
  void shouldResolveWrongMatchMethods(Object clazz)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    // Assemble
    var resolver = new ReflectionResolver(((Class<?>) clazz).getConstructor().newInstance());

    // Act
    var position = resolver.resolve("test");

    // Assert
    assertThat(position.isEmpty(), equalTo(true));
  }

  @Test
  void shouldTranslateShipName() {
    Person picardPerson = SampleModelData.PICARD_PERSON;
    picardPerson.setFavouriteShip(SampleModelData.ENTERPRISE);
    GenerationOptions generationOptions = new GenerationOptionsBuilder()
        .withTranslation((s, locale) -> {
          if (s.equals("USS Enterprise")) {
            return Optional.of("VSS Unternehmung");
          } else {
            return Optional.empty();
          }
        }).build();
    var resolver = new ReflectionResolver(picardPerson, new CustomPlaceholderRegistryImpl(), generationOptions);

    var shipName = resolver.resolve("favouriteShip");

    assertThat(shipName.get().toString(), equalTo("VSS Unternehmung"));
  }

  @Test
  void shouldResolveUUIDtoString() {
    Person picardPerson = SampleModelData.PICARD_PERSON;
    var resolver = new ReflectionResolver(picardPerson);

    var id = resolver.resolve("id");

    assertThat(id.get().toString(), equalTo(picardPerson.getId().toString()));
  }
}
