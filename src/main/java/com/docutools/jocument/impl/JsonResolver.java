package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData;
import com.google.gson.*;
import org.apache.poi.util.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonResolver implements PlaceholderResolver {

    private JsonElement jsonElement;
    private final Tika tika = new Tika();

    public JsonResolver(String json) {
        this.jsonElement = JsonParser.parseString(json);
    }

    @Override
    public Optional<PlaceholderData> resolve(String placeholderName) {
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
        List<PlaceholderResolver> list = StreamSupport.stream(spliterator,false)
                .map(JsonElement::toString)
                .map(JsonResolver::new)
                .collect(Collectors.toList());
        return Optional.of(new IterablePlaceholderData(list, list.size()));
    }

}
