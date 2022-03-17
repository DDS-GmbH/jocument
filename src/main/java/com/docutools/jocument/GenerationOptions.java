package com.docutools.jocument;

import com.docutools.jocument.formatting.PlaceholderDataFormattingOption;
import com.docutools.jocument.image.ImageStrategy;
import com.docutools.jocument.impl.ScalarPlaceholderData;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Options for generating {@link Document}s, passed to the {@link Template}s.
 *
 * @author partschi
 * @since 2021-03-24
 */
public record GenerationOptions(ImageStrategy imageStrategy,
                                List<PlaceholderDataFormattingOption> formattingOptions) {

  public Optional<PlaceholderData> tryToFormat(Locale locale, PlaceholderData placeholderData) {
    return formattingOptions.stream()
        .filter(option -> option.filter().accepts(placeholderData.getRawValue()))
        .findAny()
        .map(option -> option.formatter().format(locale, placeholderData.getRawValue()))
        .map(ScalarPlaceholderData::new);

  }

}
