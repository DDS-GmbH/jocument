package com.docutools.jocument.sample.model;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SampleModelData {

  public static final Captain PICARD;
  public static final FutureCaptain FUTURE_PICARD;
  public static final Person PICARD_PERSON = new Person("Jean-Luc", "Picard", LocalDate.of(1948, 9, 23));
  public static final List<Captain> CAPTAINS;
  public static final Ship ENTERPRISE;
  public static final Ship ENTERPRISE_WITHOUT_SERVICES;

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
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private SampleModelData() {
  }

}
