package com.docutools.jocument.sample.model;

import com.docutools.jocument.annotations.Image;

import java.nio.file.Path;
import java.util.List;

public class Captain {

  private final String name;
  private final int rank;
  private final Uniform uniform;
  private final FirstOfficer officer;
  private final List<Service> services;
  @Image
  private final Path profilePic;

  public Captain(String name, int rank, Uniform uniform, FirstOfficer officer, List<Service> services, Path profilePic) {
    this.name = name;
    this.rank = rank;
    this.uniform = uniform;
    this.officer = officer;
    this.services = services;
    this.profilePic = profilePic;
  }

  public String getName() {
    return name;
  }

  public int getRank() {
    return rank;
  }

  public Uniform getUniform() {
    return uniform;
  }

  public FirstOfficer getOfficer() {
    return officer;
  }

  public List<Service> getServices() {
    return services;
  }

  public Path getProfilePic() {
    return profilePic;
  }
}
