package com.docutools.jocument.impl;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderMapper;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.annotations.Image;
import com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Takes a {@link Object} of any type and resolves placeholder names with reflective access to its type.
 *
 * @author codecitizen
 * @see PlaceholderResolver
 * @since 2020-02-19
 */
public class FutureReflectionResolver extends ReflectionResolver {
  private static final Logger logger = LogManager.getLogger();
  private final PlaceholderMapper placeholderMapper = new PlaceholderMapperImpl();

  public FutureReflectionResolver(Object value) {
    this(value, new CustomPlaceholderRegistryImpl()); //NoOp CustomPlaceholderRegistry
  }

  public FutureReflectionResolver(Object value, CustomPlaceholderRegistry customPlaceholderRegistry) {
    super(value, customPlaceholderRegistry);
  }


  @Override
  public Optional<PlaceholderData> resolve(String placeholderName, Locale locale) {
    logger.debug("Trying to resolve placeholder {}", placeholderName);
    placeholderName = placeholderMapper.map(placeholderName);
    Optional<PlaceholderData> result = Optional.empty();
    for (String property : placeholderName.split("\\.")) {
      result = result.isEmpty()
          ? doResolve(property, locale)
          : result
          .flatMap(r -> r.stream().findAny())
          .flatMap(r -> r.resolve(property, locale));
    }
    return result;
  }

  private Optional<PlaceholderData> doResolve(String placeholderName, Locale locale) {
    try {
      if (customPlaceholderRegistry.governs(placeholderName)) {
        return customPlaceholderRegistry.resolve(placeholderName);
      }
      var property = getBeanProperty(placeholderName);
      if (property == null) {
        return Optional.empty();
      }
      if (property instanceof Future<?>) {
        property = ((Future<?>) property).get();
      }
      if (property instanceof Number number) {
        var numberFormat = findNumberFormat(placeholderName, locale);
        return Optional.of(new ScalarPlaceholderData(numberFormat.format(number)));
      } else if (property instanceof Enum || property instanceof String || ReflectionUtils.isWrapperType(property.getClass())) {
        return Optional.of(new ScalarPlaceholderData(property.toString()));
      } else if (property instanceof Collection<?> collection) {
        List<PlaceholderResolver> list = collection.stream()
            .map(object -> new FutureReflectionResolver(object, customPlaceholderRegistry))
            .collect(Collectors.toList());
        return Optional.of(new IterablePlaceholderData(list, list.size()));
      } else if (property instanceof Temporal temporal) {
        return formatTemporal(placeholderName, temporal, locale);
      } else if (property instanceof Path path && isFieldAnnotatedWith(bean.getClass(), placeholderName, Image.class)) {
        return ReflectionUtils.findFieldAnnotation(bean.getClass(), placeholderName, Image.class)
            .map(image -> new ImagePlaceholderData(path)
                .withMaxWidth(image.maxWidth()));
      }
      if (bean.equals(property)) {
        return Optional.of(new IterablePlaceholderData(List.of(new FutureReflectionResolver(bean, customPlaceholderRegistry)), 1));
      } else {
        var value = getBeanProperty(placeholderName);
        if (value instanceof Future<?>) {
          value = ((Future<?>) value).get();
        }
        return Optional.of(new IterablePlaceholderData(List.of(new FutureReflectionResolver(value, customPlaceholderRegistry)), 1));
      }
    } catch (NoSuchMethodException | IllegalArgumentException e) {
      logger.debug("Did not find placeholder {}", placeholderName);
      return Optional.empty();
    } catch (IllegalAccessException | InvocationTargetException e) {
      logger.error("Could not resolve placeholder %s".formatted(placeholderName), e);
      throw new IllegalStateException("Could not resolve placeholderName against type.", e);
    } catch (InstantiationException e) {
      logger.warn("InstantiationException when trying to resolve placeholder %s".formatted(placeholderName), e);
      return Optional.empty();
    } catch (InterruptedException e) {
      logger.warn("InterruptedException when waiting for Future placeholder %s".formatted(placeholderName), e);
      Thread.currentThread().interrupt();
      return Optional.empty();
    } catch (ExecutionException e) {
      logger.warn("Execution exception when waiting for Future placeholder %s".formatted(placeholderName), e);
      return Optional.empty();
    }
  }
}