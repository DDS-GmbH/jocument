package com.docutools.jocument.sample.model;

import java.time.LocalDate;
import java.util.List;

public record Ship(String name, Captain captain, int crew, List<Service> services, LocalDate built) {
}
