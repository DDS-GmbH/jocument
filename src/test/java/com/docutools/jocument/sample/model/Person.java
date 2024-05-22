package com.docutools.jocument.sample.model;

import com.docutools.jocument.annotations.DynamicAccessPlaceholder;
import com.docutools.jocument.annotations.Format;
import com.docutools.jocument.annotations.Translatable;
import com.docutools.jocument.impl.models.MatchPlaceholderData;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

public class Person {
  private final UUID id = UUID.randomUUID();

  private final String firstName;
  private final String lastName;
  @Format(value = "dd.MM.yyyy")
  private final LocalDate birthDate;
  private final Instant entryDate;
  @Translatable(toStringMethod = "shipName")
  private Ship favouriteShip;

  public Person(String firstName, String lastName, LocalDate birthDate) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.entryDate = birthDate.plusYears(18).atStartOfDay().toInstant(ZoneOffset.UTC);
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

  public Instant getEntryDate() {
    return entryDate;
  }

  public void setFavouriteShip(Ship favouriteShip) {
    this.favouriteShip = favouriteShip;
  }

  public Ship getFavouriteShip() {
    return favouriteShip;
  }

  public UUID getId() {
    return id;
  }

  @DynamicAccessPlaceholder(pattern = "last-used-ship")
  public Optional<Ship> getLastUsedShip(MatchPlaceholderData matchPlaceholderData) {
    return Optional.ofNullable(this.favouriteShip);
  }
}
