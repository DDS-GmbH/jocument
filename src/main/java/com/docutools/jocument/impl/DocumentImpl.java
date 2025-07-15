package com.docutools.jocument.impl;

import com.docutools.jocument.Document;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.LocaleUtil;

public abstract class DocumentImpl extends Thread implements Document {
  public static final String GERMAN_SPECIAL_CHARACTERS = "ÄäÖöÜüß";
  public static final Pattern TAG_PATTERN = Pattern.compile("\\{\\{([A-Za-z\\d" + GERMAN_SPECIAL_CHARACTERS + "@\\-/#.]+\\??)}}");
  public static final Pattern LOOP_END_PATTERN = Pattern.compile("\\{\\{/([A-Za-z\\d" + GERMAN_SPECIAL_CHARACTERS + "@\\-/#.]+\\??)}}");
  private static final Logger logger = LogManager.getLogger();
  protected final Template template;
  protected final PlaceholderResolver resolver;
  protected final GenerationOptions options;

  private boolean complete = false;
  private Path path;

  /**
   * Property constructor.
   *
   * @param template {@link Template}
   * @param resolver {@link PlaceholderResolver}
   * @param options {@link GenerationOptions}
   */
  protected DocumentImpl(Template template, PlaceholderResolver resolver, GenerationOptions options) {
    this.template = template;
    this.resolver = resolver;
    this.options = options;
  }

  protected abstract Path generate() throws IOException;

  @Override
  public void run() {
    try {
      LocaleUtil.setUserLocale(template.getLocale()); // LU is thread-local
      logger.info("Starting generating document from path {} with template {} and resolver {}", path, template, resolver);
      this.path = generate();
      logger.info("Finished generating document from path {} with template {} and resolver {}", path, template, resolver);
    }  catch (IOException e) {
      logger
          .error("Encountered IOException when generating document from path %s with template %s and resolver %s".formatted(path, template, resolver),
              e);
      throw new IllegalStateException("Got IOException when generating, probably due to template.", e);
    } catch (Exception e) {
      logger
          .error("Encountered exception when generating document from path %s with template %s and resolver %s".formatted(path, template, resolver),
              e);
    }
    complete = true;
  }

  @Override
  public void blockUntilCompletion(long millis) throws InterruptedException {
    logger.info("Waiting for completion for {} milliseconds", millis);
    super.join(millis);
  }

  @Override
  public boolean completed() {
    return complete;
  }

  @Override
  public Path getPath() {
    return path;
  }
}
