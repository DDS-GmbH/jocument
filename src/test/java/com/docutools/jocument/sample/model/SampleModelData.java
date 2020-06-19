package com.docutools.jocument.sample.model;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class SampleModelData {

  public static final Captain PICARD;
  public static final Person PICARD_PERSON = new Person("Jean-Luc", "Picard", LocalDate.of(1948, 9, 23));

  static {
    try {
      PICARD = new Captain("Jean-Luc Picard",
          4,
          Uniform.Red,
          new FirstOfficer("Riker", 3, Uniform.Red),
          List.of(new Service("USS Enterprise", Collections.singletonList(
              new PlanetServiceInfo("Mars", Collections.singletonList(new City("Nova Rojava"))))),
              new Service("US Defiant", List.of(
                  new PlanetServiceInfo("Venus", List.of(new City("Nova Parisia"), new City("Birnin Zana"))),
                  new PlanetServiceInfo("Jupiter", List.of(new City("Exarcheia"), new City("Nova Metalkova")))))),
          Path.of(SampleModelData.class.getResource("/images/picardProfile.jpg").toURI()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private SampleModelData() {
  }

}
