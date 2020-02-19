package com.docutools.jocument.sample.model;

import java.time.LocalDate;
import java.time.Period;

public class Person {

  private final String firstName;
  private final String lastName;
  private final LocalDate birthDate;

  public Person(String firstName, String lastName, LocalDate birthDate) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public String getFullName() {
    return String.format("%s %s", firstName, lastName);
  }

  public int getAge() {
    return Period.between(birthDate, LocalDate.now()).getYears();
  }
}
