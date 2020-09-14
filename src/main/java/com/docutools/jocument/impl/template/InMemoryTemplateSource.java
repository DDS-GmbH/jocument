package com.docutools.jocument.impl.template;

import com.docutools.jocument.TemplateSource;

import java.io.*;

public class InMemoryTemplateSource implements TemplateSource {

    private final byte[] data;

    public InMemoryTemplateSource(byte[] data) {
        this.data = data;
    }

    public InMemoryTemplateSource(InputStream in) throws IOException {
        try(var bos = new ByteArrayOutputStream()) {
            in.transferTo(bos);
            this.data = bos.toByteArray();
        }
    }

    @Override
    public InputStream open() throws IOException {
        return new ByteArrayInputStream(data);
    }
}
