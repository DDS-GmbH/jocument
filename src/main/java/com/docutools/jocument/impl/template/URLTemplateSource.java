package com.docutools.jocument.impl.template;

import com.docutools.jocument.TemplateSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URLTemplateSource implements TemplateSource {

    private final URL url;

    public URLTemplateSource(URI uri) throws MalformedURLException {
        this.url = uri.toURL();
    }

    public URLTemplateSource(URL url) {
        this.url = url;
    }

    @Override
    public InputStream open() throws IOException {
        return url.openStream();
    }
}
