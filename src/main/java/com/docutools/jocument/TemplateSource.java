package com.docutools.jocument;

import java.io.IOException;
import java.io.InputStream;

public interface TemplateSource {

  InputStream open() throws IOException;

}
