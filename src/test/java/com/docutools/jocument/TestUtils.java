package com.docutools.jocument;


import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public final class TestUtils {

    public static String getText(String resName) throws IOException {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(resName);
        return IOUtils.toString(inputStream);
    }

    private TestUtils() {}
}
