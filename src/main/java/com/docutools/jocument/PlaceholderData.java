package com.docutools.jocument;

import java.util.Locale;
import java.util.stream.Stream;

/**
 * Used by {@link Document}s to insert data on pre-defined placeholders.
 *
 * <p>If the {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#SCALAR} use the return value
 * of {@link this#toString()}.</p>
 *
 * @author codecitzen
 * @see com.docutools.jocument.PlaceholderResolver
 * @since 2020-02-19
 */
public interface PlaceholderData {

  /**
   * Gets the {@link com.docutools.jocument.PlaceholderType}.
   *
   * @return the {@link com.docutools.jocument.PlaceholderType}
   */
  PlaceholderType getType();

  /**
   * Streams the items represented as {@link com.docutools.jocument.PlaceholderResolver}s.
   *
   * <p>Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#SET}.</p>
   *
   * @return stream of {@link com.docutools.jocument.PlaceholderResolver}s
   */
  default Stream<PlaceholderResolver> stream() {
    throw new UnsupportedOperationException(this.getClass().toString());
  }

  /**
   * The count of {@link com.docutools.jocument.PlaceholderResolver}s.
   *
   * <p>Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#SET}.</p>
   *
   * @return count of {@link com.docutools.jocument.PlaceholderResolver}s
   */
  default long count() {
    throw new UnsupportedOperationException(this.getClass().toString());
  }

  /**
   * Transforms the given placeholder Object based on the data.
   *
   * <p>Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#CUSTOM}.</p>
   *
   * @param placeholder the placeholder
   * @param locale the {@link Locale}
   * @param options the {@link GenerationOptions}
   */
  default void transform(Object placeholder, Locale locale, GenerationOptions options) {
    throw new UnsupportedOperationException(this.getClass().toString());
  }

  /**
   * Tries to return the raw value behind the {@link PlaceholderData}. May not be implemented in all sepcifications.
   *
   * @return the raw value
   */
  default Object getRawValue() {
    throw new UnsupportedOperationException(this.getClass().toString());
  }

  /**
   * Evaluates if this {@link PlaceholderData} is a truthy value. Non-truthy values are defined as:
   *
   * <ul>
   *   <li>Empty string</li>
   *   <li>Empty collection or array</li>
   *   <li>{@code null}</li>
   *   <li>0</li>
   *   <li>{@code false}</li>
   *   <li>Empty {@link java.util.Optional}</li>
   * </ul>
   *
   * @return {@code true} if truthy
   */
  default boolean isTruthy() {
    return count() > 0;
  }

}
