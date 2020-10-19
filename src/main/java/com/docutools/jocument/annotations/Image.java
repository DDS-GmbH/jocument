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
     *
     * @return the maximum pixel width of the image to be inserted
     */
    int maxWidth() default -1;

}
