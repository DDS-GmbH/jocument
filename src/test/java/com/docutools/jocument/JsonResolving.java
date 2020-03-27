package com.docutools.jocument;

import com.docutools.jocument.impl.JsonResolver;
import com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData;
import com.docutools.jocument.sample.model.SampleModelData;
import com.docutools.jocument.sample.model.Uniform;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Resolve placeholders from a JSON.")
public class JsonResolving {

    private PlaceholderResolver resolver;

    @BeforeEach
    void setup() throws IOException {
        String json = TestUtils.getText("json/picard.json");
        resolver = new JsonResolver(json);
    }


    @Test
    @DisplayName("Resolve attributes.")
    void shouldResolveAttributes() {
        // Act
        String name = resolver.resolve("name")
                .map(PlaceholderData::toString)
                .orElseThrow();
        int rank = resolver.resolve("rank")
                .map(PlaceholderData::toString)
                .map(Integer::parseInt)
                .orElseThrow();
        Uniform uniform = resolver.resolve("uniform")
                .map(PlaceholderData::toString)
                .map(Uniform::valueOf)
                .orElseThrow();
        // Assert
        assertThat(name, equalTo(SampleModelData.PICARD.getName()));
        assertThat(rank, is(4));
        assertThat(uniform, is(Uniform.Red));
    }

    @Test
    @DisplayName("Should resolve embedded object.")
    void shouldResolveObject() {
        // Act
        PlaceholderResolver embeddedResolver = resolver.resolve("officer")
                .flatMap(r -> r.stream().findFirst())
                .orElseThrow();
        String name = embeddedResolver.resolve("name")
                .map(PlaceholderData::toString)
                .orElseThrow();
        // Act
        assertThat(name, equalTo("Riker"));
    }

    @Test
    @DisplayName("Return empty when placeholder is not defined.")
    void shouldReturnEmptyWhenPlaceholderUndefined() {
        // Act
        var data = resolver.resolve("crew");
        // Assert
        assertThat(data.isEmpty(), is(true));
    }

    @Test
    @DisplayName("Resolve collection.")
    void shouldResolveCollection() {
        // Act
        List<String> shipNames = resolver.resolve("services")
                .map(data -> data.stream()
                        .map(r -> r.resolve("shipName")
                                .map(PlaceholderData::toString)
                                .orElseThrow())
                        .collect(Collectors.toList()))
                .orElseThrow();
        // Act
        assertThat(shipNames, contains("USS Enterprise", "US Defiant"));
    }

    @Test
    @DisplayName("Resolve image")
    void shouldResolveImage() {
        // Act
        var data = resolver.resolve("profilePic");

        // Assert
        assertTrue(data.isPresent());
        assertThat(data.get(), isA(ImagePlaceholderData.class));
    }

    @Test
    @DisplayName("Throw error when parsing invalid JSON")
    void shouldThrowErrorWhenParsingInvalidJson() throws IOException {
        // Given
        final String invalidJson = TestUtils.getText("json/invalid-json.txt");

        assertThrows(JsonParseException.class, () -> new JsonResolver(invalidJson));
    }

}
