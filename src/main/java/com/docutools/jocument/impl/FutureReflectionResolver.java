package com.docutools.jocument.impl;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderMapper;
import com.docutools.jocument.PlaceholderResolver;
import java.lang.reflect.InvocationTargetException;
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
 * @author AntonOellerer
 * @see ReflectionResolver
 * @since 2021-12-21
 */
public class FutureReflectionResolver extends ReflectionResolver {
  private static final Logger logger = LogManager.getLogger();

  public FutureReflectionResolver(Object value) {
    this(value, new CustomPlaceholderRegistryImpl()); //NoOp CustomPlaceholderRegistry
  }

  public FutureReflectionResolver(Object value, CustomPlaceholderRegistry customPlaceholderRegistry) {
    super(value, customPlaceholderRegistry);
  }

  @Override
  public Optional<PlaceholderData> doResolve(String placeholderName, Locale locale) {
    try {
      if (customPlaceholderRegistry.governs(placeholderName, bean)) {
        return customPlaceholderRegistry.resolve(placeholderName, bean);
      }
      var property = getBeanProperty(placeholderName);
      if (property == null) {
        return Optional.empty();
      }
      if (property instanceof Future<?>) {
        property = ((Future<?>) property).get();
      }
      var simplePlaceholder = resolveSimplePlaceholder(property, placeholderName, locale);
      if (simplePlaceholder.isPresent()) {
        return simplePlaceholder;
      } else {
        if (property instanceof Collection<?> collection) {
          List<PlaceholderResolver> list = collection.stream()
              .map(object -> new FutureReflectionResolver(object, customPlaceholderRegistry))
              .collect(Collectors.toList());
          return Optional.of(new IterablePlaceholderData(list, list.size()));
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