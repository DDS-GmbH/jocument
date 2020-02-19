package com.docutools.jocument;

/**
 * Indicates the behavior of a {@link com.docutools.jocument.PlaceholderData} instance.
 *
 * @author codecitizen
 * @since 1.0-SNAPSHOT
 * @see com.docutools.jocument.PlaceholderData
 */
public enum PlaceholderType {
  /**
   * A single value to be inserted into the report.
   */
  SINGLE,
  /**
   * A list of values, indicating the placeholder object should be copied for each and the value injected in form of a
   * new {@link com.docutools.jocument.PlaceholderResolver}.
   */
  LIST,
  /**
   * The transformation of the placeholder object is implemented by the {@link com.docutools.jocument.PlaceholderData}
   * object.
   */
  CUSTOM
}
