package com.docutools.jocument.impl;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ClassUtils;

public class MapResolver extends PlaceholderResolver {

  private final Map<String, Object> map;
  private final CustomPlaceholderRegistry customPlaceholderRegistry; //TODO this should be refactored to a separate resolver or class-adjacent methods
  private final PlaceholderResolver parent; //FIXME add this field to FutureReflectionResolver
  private final GenerationOptions generationOptions;

  /**
   * Create a new {@link PlaceholderResolver} to resolve from {@link Map}s.
   *
   * @param map                       The {@link Map} to resolve from
   * @param customPlaceholderRegistry The custom placeholder registry to check for custom placeholders
   * @param parent                    The parent registry
   * @param generationOptions         the {@link GenerationOptions}
   */
  public MapResolver(Map<String, Object> map, CustomPlaceholderRegistry customPlaceholderRegistry, PlaceholderResolver parent,
                     GenerationOptions generationOptions) {
    this.map = map;
    this.customPlaceholderRegistry = customPlaceholderRegistry;
    this.parent = parent;
    this.generationOptions = generationOptions;
  }

  @Override
  protected Optional<PlaceholderData> doResolve(String placeholderName, Locale locale) {
    if (map.containsKey(placeholderName)) {
      var value = map.get(placeholderName);
      if (value instanceof Map childMap) {
        return Optional.of(new IterablePlaceholderData(new MapResolver(childMap, customPlaceholderRegistry, this, generationOptions)));
      } else if (ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
        return Optional.of(new ScalarPlaceholderData<>(value));
      } else {
        var futureResolver =
            new FutureReflectionResolver(value, customPlaceholderRegistry, options, 10L); //TODO find way to decouple timout time from resolver
        return futureResolver.doResolve(placeholderName, locale);
      }
    }
    return Optional.empty();
  }
}
