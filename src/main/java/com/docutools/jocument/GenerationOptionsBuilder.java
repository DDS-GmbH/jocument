package com.docutools.jocument;

import com.docutools.jocument.formatting.LocalisedPlaceholderDataFormatter;
import com.docutools.jocument.formatting.PlaceholderDataFormatter;
import com.docutools.jocument.formatting.PlaceholderDataFormattingOption;
import com.docutools.jocument.image.DefaultImageStrategy;
import com.docutools.jocument.image.ImageStrategy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Builder for {@link GenerationOptions}, use {@link this#buildDefaultOptions()} to take all default options for {@link GenerationOptions}.
 *
 * @author partschi
 * @since 2021-03-24
 */
public final class GenerationOptionsBuilder {

  public static GenerationOptions buildDefaultOptions() {
    return new GenerationOptionsBuilder().build();
  }

  private ImageStrategy imageStrategy;
  private final List<PlaceholderDataFormattingOption> formattingOptions = new ArrayList<>();
  private BiFunction<String, Locale, Optional<String>> translationFunction = null;
  private Duration waitTime = Duration.ofSeconds(30);

  public GenerationOptionsBuilder() {
    this.imageStrategy = DefaultImageStrategy.instance();
  }

  public GenerationOptionsBuilder withImageStrategy(ImageStrategy imageStrategy) {
    this.imageStrategy = Objects.requireNonNull(imageStrategy);
    return this;
  }

  public <T> GenerationOptionsBuilder format(Class<T> filter, PlaceholderDataFormatter<T> formatter) {
    return format(filter, (l, v) -> formatter.format(v));
  }

  public <T> GenerationOptionsBuilder format(Class<T> filter, LocalisedPlaceholderDataFormatter<T> formatter) {
    formattingOptions.add(new PlaceholderDataFormattingOption<>(obj -> obj != null && obj.getClass().isAssignableFrom(filter), formatter));
    return this;
  }

  public GenerationOptionsBuilder withTranslation(BiFunction<String, Locale, Optional<String>> translationFunction) {
    this.translationFunction = translationFunction;
    return this;
  }

  public GenerationOptionsBuilder withMaximumWaitTime(Duration waitTime) {
    this.waitTime = waitTime;
    return this;
  }

  public GenerationOptions build() {
    return new GenerationOptions(imageStrategy, waitTime, formattingOptions, translationFunction);
  }

}
