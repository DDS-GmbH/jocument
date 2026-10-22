package com.docutools.jocument;

import com.docutools.jocument.formatting.PlaceholderDataFormattingOption;
import com.docutools.jocument.image.ImageStrategy;
import com.docutools.jocument.impl.ScalarPlaceholderData;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Options for generating {@link Document}s, passed to the {@link Template}s.
 *
 * @author partschi
 * @since 2021-03-24
 */
public record GenerationOptions(ImageStrategy imageStrategy,
                                Duration maximumWaitTime,
                                List<PlaceholderDataFormattingOption> formattingOptions,
                                BiFunction<String, Locale, Optional<String>> translationFunction,
                                Optional<MimeType> documentMimeType,
                                Optional<Path> tempDirectory) {

  public GenerationOptions(ImageStrategy imageStrategy,
                           Duration maximumWaitTime,
                           List<PlaceholderDataFormattingOption> formattingOptions,
                           BiFunction<String, Locale, Optional<String>> translationFunction,
                           Optional<MimeType> documentMimeType) {
    this(imageStrategy, maximumWaitTime, formattingOptions, translationFunction, documentMimeType, Optional.empty());
  }

  public GenerationOptions(ImageStrategy imageStrategy,
                           Duration maximumWaitTime,
                           List<PlaceholderDataFormattingOption> formattingOptions,
                           BiFunction<String, Locale, Optional<String>> translationFunction) {
    this(imageStrategy, maximumWaitTime, formattingOptions, translationFunction, Optional.empty());
  }

  /**
   * Creates a temporary file in the {@link #tempDirectory()}, or in the default temporary-file directory if none is set.
   *
   * @param suffix The suffix of the file name, e.g. {@code ".docx"}
   * @return The path to the created file
   * @throws IOException If the file could not be created
   */
  public Path createTempFile(String suffix) throws IOException {
    return TempFiles.create(tempDirectory.orElse(null), suffix);
  }

  /**
   * Try to format a {@link PlaceholderData} with the given {@link Locale}.
   *
   * @param locale          The locale to use for formatting
   * @param placeholderData The data to format
   * @return An {@link Optional} containing the formatted {@link PlaceholderData} if successful, {@link Optional#empty()} otherwise.
   */
  public Optional<PlaceholderData> tryToFormat(Locale locale, PlaceholderData placeholderData) {
    return formattingOptions.stream()
        .filter(option -> option.filter().accepts(placeholderData.getRawValue()))
        .findFirst()
        .map(option -> option.formatter().format(locale, placeholderData.getRawValue()))
        .map(ScalarPlaceholderData::new);

  }

  /**
   * Try to format an object with the given {@link Locale}.
   *
   * @param locale   The locale to use for formatting
   * @param toFormat The data to format
   * @return An {@link Optional} containing the formatted {@link String} if successful, {@link Optional#empty()} otherwise.
   */
  public <T> Optional<String> tryToFormat(Locale locale, T toFormat) {
    return formattingOptions.stream()
        .filter(option -> option.filter().accepts(toFormat))
        .findFirst()
        .map(option -> option.formatter().format(locale, toFormat));

  }

  /**
   * Translate a given term to the given locale.
   *
   * @param term   The term to translate
   * @param locale The locale to translate the term to
   * @return An {@link Optional} containing the translated term if successful, {@link Optional#empty()} otherwise
   */
  public Optional<String> translate(String term, Locale locale) {
    if (translationFunction != null) {
      return translationFunction.apply(term, locale);
    }
    return Optional.empty();
  }

}
