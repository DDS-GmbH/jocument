package com.docutools.jocument;

import java.util.Locale;
import java.util.Optional;
import org.apache.poi.util.LocaleUtil;

/**
 * Resolves string / placeholder names to {@link com.docutools.jocument.PlaceholderData}. Used in
 * {@link Document}s.
 *
 * @author codecitizen
 * @see com.docutools.jocument.PlaceholderData
 * @see Document
 * @since 2020-02-19
 */
public interface PlaceholderResolver {

  /**
   * Resolves the given placeholder name String to a {@link com.docutools.jocument.PlaceholderData}.
   *
   * @param placeholderName the name of the paceholder
   * @return if the name could've been resolved the {@link com.docutools.jocument.PlaceholderData}
   */
  default Optional<PlaceholderData> resolve(String placeholderName) {
    return resolve(placeholderName, LocaleUtil.getUserLocale());
  }

  /**
   * Resolves the given placeholder name String to a localised {@link com.docutools.jocument.PlaceholderData}.
   *
   * @param placeholderName the name of the placeholder
   * @param locale          the localisation settings
   * @return if the name could've been resolved the localised {@link com.docutools.jocument.PlaceholderData}
   */
  Optional<PlaceholderData> resolve(String placeholderName, Locale locale);

}
