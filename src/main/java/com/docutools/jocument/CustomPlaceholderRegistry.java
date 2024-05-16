package com.docutools.jocument;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public interface CustomPlaceholderRegistry {
  void addHandler(String placeholder, Class<? extends PlaceholderData> customWordPlaceholderDataClass);

  Optional<PlaceholderData> resolve(String placeholder, Object object)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassCastException;

  boolean governs(String placeholderName, Object object);

  default boolean governs(String placeholderName, Object bean, Optional<MimeType> mimeType) {
    return governs(placeholderName, bean);
  }
}
