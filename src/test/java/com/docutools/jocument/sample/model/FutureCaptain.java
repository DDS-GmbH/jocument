package com.docutools.jocument.sample.model;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record FutureCaptain(CompletableFuture<String> name,
                            CompletableFuture<Integer> rank,
                            CompletableFuture<Uniform> uniform,
                            CompletableFuture<FirstOfficer> officer,
                            CompletableFuture<List<Service>> services,
                            CompletableFuture<Path> profilePic) {
}
