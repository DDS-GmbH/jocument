package com.docutools.jocument;

import java.util.stream.Stream;

/**
 * Used by {@link com.docutools.jocument.Report}s to insert data on pre-defined placeholders.
 *
 * If the {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#SINGLE} use the return value
 * of {@link this#toString()}.
 *
 * @author codecitzen
 * @since 1.0-SNAPSHOT
 * @see com.docutools.jocument.PlaceholderResolver
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
   * Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#LIST}.
   *
   * @return stream of {@link com.docutools.jocument.PlaceholderResolver}s
   */
  default Stream<PlaceholderResolver> stream() {
    throw new UnsupportedOperationException();
  }

  /**
   * The count of {@link com.docutools.jocument.PlaceholderResolver}s.
   *
   * Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#LIST}.
   *
   * @return count of {@link com.docutools.jocument.PlaceholderResolver}s
   */
  default long count() {
    throw new UnsupportedOperationException();
  }

  /**
   * Transforms the given placeholder Object based on the data.
   *
   * Only used when {@link this#getType()} returns {@link com.docutools.jocument.PlaceholderType#CUSTOM}.
   *
   * @param placeholder the placeholder
   */
  default void transform(Object placeholder) {
    throw new UnsupportedOperationException();
  }

}
