package com.docutools.jocument;

import com.docutools.jocument.image.DefaultImageStrategy;
import com.docutools.jocument.image.ImageStrategy;
import java.util.Objects;

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

  public GenerationOptionsBuilder() {
    this.imageStrategy = DefaultImageStrategy.instance();
  }

  public GenerationOptionsBuilder withImageStrategy(ImageStrategy imageStrategy) {
    this.imageStrategy = Objects.requireNonNull(imageStrategy);
    return this;
  }

  public GenerationOptions build() {
    return new GenerationOptions(imageStrategy);
  }

}
