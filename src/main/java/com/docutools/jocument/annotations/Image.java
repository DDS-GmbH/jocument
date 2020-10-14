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
}
