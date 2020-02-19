package com.docutools.jocument.sample.model;

public class FirstOfficer {

  private final String name;
  private final int rank;
  private final Uniform uniform;

  public FirstOfficer(String name, int rank, Uniform uniform) {
    this.name = name;
    this.rank = rank;
    this.uniform = uniform;
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
}
