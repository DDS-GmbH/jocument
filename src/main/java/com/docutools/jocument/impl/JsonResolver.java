package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.poi.util.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Takes a {@link java.lang.String} or {@link java.net.URL} of a JSON and resolves placeholder names.
 *
 * @author betorcs
 * @see com.docutools.jocument.PlaceholderResolver
 * @since 1.0-SNAPSHOT
 */
public class JsonResolver implements PlaceholderResolver {

    private JsonElement jsonElement;
    private final Tika tika = new Tika();

    /**
     * Creates a JsonResolver using the given JSON string.
     *
     * @param json JSON string content.
     * @throws JsonParseException if the specified text is not valid JSON
     */
    public JsonResolver(String json) {
        this.jsonElement = JsonParser.parseString(json);
    }

    /**
     * Creates a JsonResolver using the given URL from a JSON string.
     *
     * @param url A valid JSON url.
     * @throws IOException        if an I/O exception occurs.
     * @throws JsonParseException if the downloaded text is not valid JSON
     */
    public JsonResolver(URL url) throws IOException {
        try (InputStream stream = url.openStream()) {
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(stream)));
            this.jsonElement = JsonParser.parseReader(reader);
        }
    }

    @Override
    public Optional<PlaceholderData> resolve(String placeholderName) {
        return resolve(placeholderName, Locale.getDefault());
    }

    @Override
    public Optional<PlaceholderData> resolve(String placeholderName, Locale locale) {
        if (jsonElement.isJsonObject()) {
            return fromObject(placeholderName, jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonArray()) {
            return fromArray(jsonElement.getAsJsonArray());
        }
        return Optional.empty();
    }

    private Optional<PlaceholderData> fromObject(String placeholderName, JsonObject jsonObject) {
        if (!jsonObject.has(placeholderName))
            return Optional.empty();

        JsonElement element = jsonObject.get(placeholderName);
        if (element.isJsonPrimitive()) {
            return fromPrimitive(element.getAsJsonPrimitive());
        } else if (element.isJsonArray()) {
            return fromArray(element.getAsJsonArray());
        } else if (element.isJsonObject()) {
            String json = element.toString();
            return Optional.of(new IterablePlaceholderData(List.of(new JsonResolver(json)), 1));
        }

        return Optional.empty();
    }

    private Optional<PlaceholderData> fromPrimitive(JsonPrimitive primitive) {
        String data = primitive.getAsString();
        if (isImage(data)) {
            return fromUrlContent(data)
                    .map(ImagePlaceholderData::new);
        }
        return Optional.of(new ScalarPlaceholderData(data));
    }

    private boolean isImage(String data) {
        try {
            String detected = tika.detect(data);
            MediaType mediaType = MediaType.parse(detected);
            return mediaType != null && "image".equals(mediaType.getType());
        } catch (IllegalStateException ignored) {
            return false;
        }

    }

    private Optional<Path> fromUrlContent(String url) {
        try (InputStream stream = new URL(url).openStream()) {
            Path tmp = Files.createTempFile("jocument-", ".dat");
            IOUtils.copy(stream, tmp.toFile());
            return Optional.of(tmp);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<PlaceholderData> fromArray(JsonArray jsonArray) {
        Spliterator<JsonElement> spliterator = jsonArray.spliterator();
        List<PlaceholderResolver> list = StreamSupport.stream(spliterator, false)
                .map(JsonElement::toString)
                .map(JsonResolver::new)
                .collect(Collectors.toList());
        return Optional.of(new IterablePlaceholderData(list, list.size()));
    }

}
