package com.docutools.jocument;

import com.docutools.jocument.image.ImageStrategy;

/**
 * Options for generating {@link Document}s, passed to the {@link Template}s.
 *
 * @author partschi
 * @since 2021-03-24
 */
public record GenerationOptions(ImageStrategy imageStrategy) {
}
