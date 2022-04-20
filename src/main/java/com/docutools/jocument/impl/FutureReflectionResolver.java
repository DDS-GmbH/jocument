package com.docutools.jocument.impl;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
  private final long maximumWaitTime;

  public FutureReflectionResolver(Object value) {
    this(value, new CustomPlaceholderRegistryImpl()); //NoOp CustomPlaceholderRegistry
  }

  public FutureReflectionResolver(Object value, CustomPlaceholderRegistry customPlaceholderRegistry) {
    this(value, customPlaceholderRegistry, Long.MAX_VALUE);
  }

  public FutureReflectionResolver(Object value,
                                  CustomPlaceholderRegistry customPlaceholderRegistry,
                                  long maximumWaitTimeSeconds) {
    this(value, customPlaceholderRegistry, GenerationOptionsBuilder.buildDefaultOptions(), maximumWaitTimeSeconds);
  }

  /**
   * Create a new Future Reflection Resolver.
   *
   * @param value                     The value to resolve upon
   * @param customPlaceholderRegistry A custom placeholder registry for custom placeholders
   * @param options                   Generation options
   * @param maximumWaitTimeSeconds    The maximum wait time to wait for futures
   */
  public FutureReflectionResolver(Object value,
                                  CustomPlaceholderRegistry customPlaceholderRegistry,
                                  GenerationOptions options,
                                  long maximumWaitTimeSeconds) {
    super(value, customPlaceholderRegistry);
    this.maximumWaitTime = maximumWaitTimeSeconds;
    setOptions(options);
  }

  @Override
  public Optional<PlaceholderData> doReflectiveResolve(String placeholderName, Locale locale) {
    try {
      if (customPlaceholderRegistry.governs(placeholderName, bean)) {
        logger.info("Placeholder {} handled by custom registry", placeholderName);
        return customPlaceholderRegistry.resolve(placeholderName, bean);
      }
      var property = getBeanProperty(placeholderName);
      if (property == null) {
        logger.debug("Placeholder {} could not be translated into a property", placeholderName);
        return Optional.empty();
      }
      property = resolveNonFinalValue(property, placeholderName);
      var simplePlaceholder = resolveSimplePlaceholder(property, placeholderName, locale);
      if (simplePlaceholder.isPresent()) {
        logger.debug("Placeholder {} resolved to simple placeholder", placeholderName);
        return simplePlaceholder;
      } else {
        if (property instanceof Collection<?> collection) {
          logger.debug("Placeholder {} resolved to collection", placeholderName);
          List<PlaceholderResolver> list = collection.stream()
              .map(object -> new FutureReflectionResolver(object, customPlaceholderRegistry, options, maximumWaitTime))
              .collect(Collectors.toList());
          return Optional.of(new IterablePlaceholderData(list, list.size()));
        }
        if (bean.equals(property)) {
          logger.debug("Placeholder {} resolved to the parent object", placeholderName);
          return Optional.of(
              new IterablePlaceholderData(List.of(new FutureReflectionResolver(bean, customPlaceholderRegistry, options, maximumWaitTime)), 1));
        } else {
          var value = getBeanProperty(placeholderName);
          logger.debug("Resolved placeholder {} to the bean property {}", placeholderName, value);
          value = resolveNonFinalValue(value, placeholderName);
          return Optional.of(
              new IterablePlaceholderData(List.of(new FutureReflectionResolver(value, customPlaceholderRegistry, options, maximumWaitTime)), 1));
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
    } catch (TimeoutException e) {
      logger.warn("Timeout exception when waiting for Future placeholder {}", placeholderName, e);
      return Optional.empty();
    } catch (EmptyOptionalException e) {
      logger.warn("Placeholder {} property is an empty optional", e.getMessage());
      return Optional.empty();
    }
  }

  private Object resolveNonFinalValue(Object property, String placeholderName)
      throws ExecutionException, InterruptedException, TimeoutException, EmptyOptionalException {
    var resolvedProperty = property;
    if (property instanceof Future<?> future) {
      logger.debug("Placeholder {} property is a future, getting it", placeholderName);
      resolvedProperty = future.get(maximumWaitTime, TimeUnit.SECONDS);
      logger.debug("Placeholder {} property future retrieved", placeholderName);
    }
    if (property instanceof Optional<?> optional) {
      logger.debug("Placeholder {} property is an optional, getting it", placeholderName);
      if (optional.isEmpty()) {
        throw new EmptyOptionalException(placeholderName);
      } else {
        resolvedProperty = optional.get();
        logger.debug("Optional placeholder {} property contained {}", placeholderName, property);
      }
    }
    return resolvedProperty;
  }
}