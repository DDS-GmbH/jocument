package com.docutools.jocument.sample.model;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.IterablePlaceholderData;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.impl.excel.util.PlaceholderDataFactory;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class SampleModelData {

  public static final Captain PICARD;
  public static final FutureCaptain FUTURE_PICARD;
  public static final Person PICARD_PERSON = new Person("Jean-Luc", "Picard", LocalDate.of(1948, 9, 23));
  public static final Person PICARD_NULL = new Person(null, "Picard", LocalDate.of(1948, 9, 23));
  public static final List<Captain> CAPTAINS;
  public static final Ship ENTERPRISE;
  public static final Ship ENTERPRISE_WITHOUT_SERVICES;
  public static final Planet PLANET;
  public static final Army ARMY;
  private static final Random RANDOM = new Random();
  public static final Person LINEBREAK_NAME_PERSON = new Person("Tyron \n Socci", "Mignon \n Ellworths", LocalDate.now());

  static {
    try {
      var services = List.of(new Service("USS Enterprise", Collections.singletonList(
              new PlanetServiceInfo("Mars", Collections.singletonList(new City("Nova Rojava"))))),
          new Service("US Defiant", List.of(
              new PlanetServiceInfo("Venus", List.of(new City("Nova Parisia"), new City("Birnin Zana"))),
              new PlanetServiceInfo("Jupiter", List.of(new City("Exarcheia"), new City("Nova Metalkova"))))));
      PICARD = new Captain("Jean-Luc Picard",
          4,
          Uniform.Red,
          new FirstOfficer("Riker", 3, Uniform.Red),
          services,
          Path.of(SampleModelData.class.getResource("/images/picardProfile.jpg").toURI()),
          "sympathetic");
      CAPTAINS = List.of(PICARD);
      ENTERPRISE = new Ship("USS Enterprise", PICARD, 5, services, LocalDate.now(), Optional.of("Mos Eisley"));
      FUTURE_PICARD = new FutureCaptain(CompletableFuture.completedFuture("Jean-Luc Picard"),
          CompletableFuture.completedFuture(4),
          CompletableFuture.completedFuture(Uniform.Red),
          CompletableFuture.completedFuture(new FirstOfficer("Riker", 3, Uniform.Red)),
          CompletableFuture.completedFuture(services),
          CompletableFuture.completedFuture(Path.of(SampleModelData.class.getResource("/images/picardProfileLarge.jpg").toURI())));
      ENTERPRISE_WITHOUT_SERVICES = new Ship("USS Enterprise", PICARD, 5, Collections.emptyList(), LocalDate.now(), Optional.empty());
      PLANET = new Planet(new PlaceholderDataFactory() {
        @Override
        public PlaceholderData create(CustomPlaceholderRegistry customPlaceholderRegistry, GenerationOptions options, PlaceholderResolver parent) {
          return new IterablePlaceholderData(new ReflectionResolver(PICARD, customPlaceholderRegistry, options, parent));
        }
      });
      ARMY = new Army(List.of(createCaptain(), createCaptain(), createCaptain(), createCaptain()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static Captain createCaptain() {
    return new Captain("Captain #%s".formatted(RANDOM.nextInt()), RANDOM.nextInt(), Uniform.values()[RANDOM.nextInt(Uniform.values().length - 1)],
        null, Collections.emptyList(), null, "");
  }

  private SampleModelData() {
  }

}
