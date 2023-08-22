package com.docutools.jocument.impl.excel.util;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;

public interface PlaceholderDataFactory {
  PlaceholderData create(CustomPlaceholderRegistry customPlaceholderRegistry,
                         GenerationOptions options,
                         PlaceholderResolver parent);
}
