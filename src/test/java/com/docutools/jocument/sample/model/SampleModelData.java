package com.docutools.jocument.sample.model;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class SampleModelData {

  public static final Captain PICARD;

  static {
    try {
      PICARD = new Captain("Jean-Luc Picard",
                4, Uniform.Red, new FirstOfficer("Riker", 3, Uniform.Red),
                List.of(new Service("USS Enterprise"), new Service("US Defiant")),
                Path.of(SampleModelData.class.getResource("/images/picardProfile.jpg").toURI()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static final Person PICARD_PERSON = new Person("Jean-Luc", "Picard",LocalDate.of(1948, 9,23));

  private SampleModelData() {
  }

}
