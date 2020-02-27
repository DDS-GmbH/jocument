package com.docutools.jocument;

import java.util.Optional;

/**
 * Resolves string / placeholder names to {@link com.docutools.jocument.PlaceholderData}. Used in
 * {@link Document}s.
 *
 * @author codecitizen
 * @since 1.0-SNAPSHOT
 * @see com.docutools.jocument.PlaceholderData
 * @see Document
 */
public interface PlaceholderResolver {

  /**
   * Resolves the given placeholder name String to a {@link com.docutools.jocument.PlaceholderData}.
   *
   * @param placeholderName the name of the paceholder
   * @return if the name could've been resolved the {@link com.docutools.jocument.PlaceholderData}
   */
  Optional<PlaceholderData> resolve(String placeholderName);

}
