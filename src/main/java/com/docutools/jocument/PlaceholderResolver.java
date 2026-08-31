package com.docutools.jocument;

import com.docutools.jocument.impl.ScalarPlaceholderData;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.poi.util.LocaleUtil;

/**
 * Resolves string / placeholder names to {@link com.docutools.jocument.PlaceholderData}. Used in
 * {@link Document}s.
 *
 * <p>A {@link PlaceholderResolver} should not be consdiered reusable!</p>
 *
 * @author codecitizen
 * @see com.docutools.jocument.PlaceholderData
 * @see Document
 * @since 2020-02-19
 */
public abstract class PlaceholderResolver {

  private static final Pattern INVISIBLE_CHARS = Pattern.compile("[\\p{Cf}\\h\\s]");

  protected GenerationOptions options = GenerationOptionsBuilder.buildDefaultOptions();

  public final void setOptions(GenerationOptions options) {
    this.options = Objects.requireNonNull(options);
  }

  /**
   * Resolves the given placeholder name String to a {@link com.docutools.jocument.PlaceholderData}.
   *
   * @param placeholderName the name of the paceholder
   * @return if the name could've been resolved the {@link com.docutools.jocument.PlaceholderData}
   */
  public Optional<PlaceholderData> resolve(String placeholderName) {
    return resolve(placeholderName, LocaleUtil.getUserLocale());
  }

  /**
   * Resolves the given placeholder name String to a localised {@link com.docutools.jocument.PlaceholderData}.
   *
   * @param placeholderName the name of the placeholder
   * @param locale          the localisation settings
   * @return if the name could've been resolved the localised {@link com.docutools.jocument.PlaceholderData}
   */
  public Optional<PlaceholderData> resolve(String placeholderName, Locale locale) {
    return doResolve(removeInvisibleChars(placeholderName), locale)
        .map(placeholderData -> format(locale, placeholderData));
  }

  private static String removeInvisibleChars(String placeholderName) {
    return INVISIBLE_CHARS.matcher(placeholderName).replaceAll("");
  }

  private PlaceholderData format(Locale locale, PlaceholderData original) {
    if (original instanceof ScalarPlaceholderData<?>) {
      return options.tryToFormat(locale, original).orElse(original);
    }
    return original;
  }

  protected abstract Optional<PlaceholderData> doResolve(String placeholderName, Locale locale);

  /**
   * Overwrite of the {@link Object#toString()} method, forcing any subclass to implement it,
   * since its required for resolving {@link com.docutools.jocument.impl.IterablePlaceholderData}
   * when no loop-end is present.
   *
   * @return a {@link String}
   */
  @Override
  public abstract String toString();
}
