package com.docutools.jocument.annotations;

import com.docutools.jocument.impl.models.MatchPlaceholderData;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * Allows to define a regular expression pattern for a method to be resolved in a {@link com.docutools.jocument.impl.ReflectionResolver} if the
 * placeholder name passed to {@link com.docutools.jocument.impl.ReflectionResolver#resolve(String, Locale)} matches against the {@link
 * this#pattern()}.
 *
 * <p>Can be applied to a public method taking a {@link MatchPlaceholderData}, returning an {@link java.util.Optional} of {@link Object}.
 *
 * @author amp
 * @since 2022-03-01
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicAccessPlaceholder {
  /**
   * A RegEx pattern.
   *
   * @return {@link String}
   */
  String pattern();
}
