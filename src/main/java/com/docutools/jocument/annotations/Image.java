package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates to {@link com.docutools.jocument.impl.ReflectionResolver} to use the
 * {@link com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData} to resolve this field.
 *
 * @author codecitizen
 * @see com.docutools.jocument.impl.ReflectionResolver
 * @since 2020-02-19
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Image {

  /**
   * The maximum pixel width the image should be inserted with. If the image is smaller won't affect it.
   * A value of 0 is not permitted and will be ignored.
   *
   * @return the maximum pixel width of the image to be inserted
   */
  int maxWidth() default -1;

  /**
   * The maximum pixel height the image should be inserted with. If the image is smaller won't affect it.
   * A value of 0 is not permitted and will be ignored.
   *
   * @return the maximum pixel height of the image to be inserted
   */
  int maxHeight() default -1;

  /**
   * Whether the resolved image should be deleted from the file system after insertion.
   *
   * @return whether the image should be deleted after insertion
   */
  boolean deleteAfterInsertion() default true;

}
