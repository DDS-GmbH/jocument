package com.docutools.jocument.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates to {@link com.docutools.jocument.impl.ReflectionResolver} to use the
 * {@link com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData} to resolve this field.
 *
 * @author codecitizen
 * @since 1.0-SNAPSHOT
 * @see com.docutools.jocument.impl.ReflectionResolver
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Image {
}
