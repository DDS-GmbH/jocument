package com.docutools.jocument.sample.model;

import java.util.List;
import java.util.Objects;

public class Service {

  private final String shipName;

  private final List<PlanetServiceInfo> visitedPlanets;

  public Service(String shipName, List<PlanetServiceInfo> visitedPlanets) {
    this.shipName = shipName;
    this.visitedPlanets = visitedPlanets;
  }

  public String getShipName() {
    return shipName;
  }

  public List<PlanetServiceInfo> getVisitedPlanets() {
    return visitedPlanets;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Service service = (Service) o;
    return Objects.equals(shipName, service.shipName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shipName);
  }

  @Override
  public String toString() {
    return "Service{" +
            "shipName='" + shipName + '\'' +
            '}';
  }
}
