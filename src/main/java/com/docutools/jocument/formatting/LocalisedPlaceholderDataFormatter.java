package com.docutools.jocument.formatting;

import java.util.Locale;

public interface LocalisedPlaceholderDataFormatter<T> {

  String format(Locale locale, T value);

}
