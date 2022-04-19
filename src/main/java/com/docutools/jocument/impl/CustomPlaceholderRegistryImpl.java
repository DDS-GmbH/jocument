package com.docutools.jocument.impl;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.PlaceholderData;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomPlaceholderRegistryImpl implements CustomPlaceholderRegistry {
  private final Map<String, Class<? extends PlaceholderData>> customWordPlaceholderDataMap = new HashMap<>();

  @Override
  public void addHandler(String placeholder, Class<? extends PlaceholderData> customWordPlaceholderDataClass) {
    customWordPlaceholderDataMap.put(placeholder, customWordPlaceholderDataClass);
  }

  @Override
  public Optional<PlaceholderData> resolve(String placeholder, Object unused)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    if (customWordPlaceholderDataMap.containsKey(placeholder)) {
      return Optional.of(customWordPlaceholderDataMap.get(placeholder).getConstructor().newInstance());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean governs(String placeholderName, Object unused) {
    return customWordPlaceholderDataMap.containsKey(placeholderName);
  }
}
