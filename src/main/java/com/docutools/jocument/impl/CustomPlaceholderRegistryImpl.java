package com.docutools.jocument.impl;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomPlaceholderRegistryImpl implements CustomPlaceholderRegistry {
  private final Map<String, Class<? extends CustomWordPlaceholderData>> customWordPlaceholderDataMap = new HashMap<>();

  @Override
  public void addHandler(String placeholder, Class<? extends CustomWordPlaceholderData> customWordPlaceholderDataClass) {
    customWordPlaceholderDataMap.put(placeholder, customWordPlaceholderDataClass);
  }

  @Override
  public Optional<PlaceholderData> resolve(String placeholder)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    if (customWordPlaceholderDataMap.containsKey(placeholder)) {
      return Optional.of(customWordPlaceholderDataMap.get(placeholder).getConstructor().newInstance());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean governs(String placeholderName) {
    return customWordPlaceholderDataMap.containsKey(placeholderName);
  }
}
