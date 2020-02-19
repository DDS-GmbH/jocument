package com.docutools.jocument;

import java.nio.file.Path;

public interface Report {

  void blockUntilCompletion(long time) throws InterruptedException;

  boolean completed();

  Path getPath();
}
