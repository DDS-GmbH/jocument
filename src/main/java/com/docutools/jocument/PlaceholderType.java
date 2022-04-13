package com.docutools.jocument;

/**
 * Indicates the behavior of a {@link com.docutools.jocument.PlaceholderData} instance.
 *
 * @author codecitizen
 * @see com.docutools.jocument.PlaceholderData
 * @since 2020-02-19
 */
public enum PlaceholderType {
  /**
   * A scalar/primitive value to be inserted into the document.
   */
  SCALAR,
  /**
   * A set of values, indicating the placeholder object should be copied for each and the value injected in form of a
   * new {@link com.docutools.jocument.PlaceholderResolver}.
   */
  SET,
  /**
   * The transformation of the placeholder object is implemented by the {@link com.docutools.jocument.PlaceholderData}
   * object.
   */
  CUSTOM
}
