package com.docutools.jocument.sample.model;

import java.util.List;

public class PlanetServiceInfo {
    private final String planetName;
    private final List<City> visitedCities;

    public PlanetServiceInfo(String planetName, List<City> visitedCities) {
        this.planetName = planetName;
        this.visitedCities = visitedCities;
    }

    public String getPlanetName() {
        return planetName;
    }

    public List<City> getVisitedCities() {
        return visitedCities;
    }
}
