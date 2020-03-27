package com.docutools.jocument;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public final class TestUtils {

    public static String getText(String resName) throws IOException {
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resName)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .collect(Collectors.joining());
            }
        }
    }

    private TestUtils() {
    }
}
