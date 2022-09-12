package com.docutools.jocument.impl.models;

import com.docutools.jocument.GenerationOptions;
import java.util.Locale;

public record MatchPlaceholderData(String placeholder, Locale locale, GenerationOptions generationOptions) {
}
