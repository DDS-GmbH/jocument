package com.docutools.jocument;

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
    throw new UnsupportedOperationException();
  }

  /**
   * The count of {@link com.docutools.jocument.PlaceholderResolver}s.
   *
   * <p>Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#SET}.</p>
   *
   * @return count of {@link com.docutools.jocument.PlaceholderResolver}s
   */
  default long count() {
    throw new UnsupportedOperationException();
  }

  /**
   * Transforms the given placeholder Object based on the data.
   *
   * <p>Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#CUSTOM}.</p>
   *
   * @param placeholder the placeholder
   * @param options the {@link GenerationOptions}
   */
  default void transform(Object placeholder, GenerationOptions options) {
    throw new UnsupportedOperationException();
  }

  /**
   * Evaluates if this {@link PlaceholderData} is a truphy value. Non-truphy values are defined as:
   *
   * <ul>
   *   <li>Empty string</li>
   *   <li>Empty collection or array</li>
   *   <li>{@code null}</li>
   *   <li>0</li>
   *   <li>{@code false}</li>
   * </ul>
   *
   * @return {@code true} if truphy
   */
  default boolean isTruthy() {
    return count() > 0;
  }

}
