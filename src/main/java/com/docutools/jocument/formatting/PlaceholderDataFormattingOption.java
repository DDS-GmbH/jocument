package com.docutools.jocument.formatting;

import com.docutools.jocument.PlaceholderData;

/**
 * An option for formatting {@link PlaceholderData} by a specific {@link PlaceholderDataFormatter}, given the {@link PlaceholderDataFormatterFilter}
 * accepts it.
 *
 * @param <T> the {@link PlaceholderData#getRawValue()} type.
 */
public record PlaceholderDataFormattingOption<T>(PlaceholderDataFormatterFilter filter,
                                                 LocalisedPlaceholderDataFormatter<T> formatter) {
}
