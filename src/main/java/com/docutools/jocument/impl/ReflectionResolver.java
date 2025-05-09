package com.docutools.jocument.impl;

import com.docutools.jocument.CustomPlaceholderRegistry;
import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderMapper;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.annotations.DynamicAccessPlaceholder;
import com.docutools.jocument.annotations.Format;
import com.docutools.jocument.annotations.Image;
import com.docutools.jocument.annotations.MatchPlaceholder;
import com.docutools.jocument.annotations.Money;
import com.docutools.jocument.annotations.Numeric;
import com.docutools.jocument.annotations.Percentage;
import com.docutools.jocument.annotations.Translatable;
import com.docutools.jocument.impl.excel.util.PlaceholderDataFactory;
import com.docutools.jocument.impl.models.MatchPlaceholderData;
import com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Takes a {@link java.lang.Object} of any type and resolves placeholder names with reflective access to its type.
 *
 * @author codecitizen
 * @see com.docutools.jocument.PlaceholderResolver
 * @since 2020-02-19
 */
public class ReflectionResolver extends PlaceholderResolver {
  private static final String SELF_REFERENCE = "this";
  private static final String PARENT_SYMBOL = "@";

  private static final Logger logger = LogManager.getLogger();

  private final Object bean;
  private final CustomPlaceholderRegistry customPlaceholderRegistry;
  private final PropertyUtilsBean pub = new PropertyUtilsBean();
  private final PlaceholderMapper placeholderMapper = new PlaceholderMapperImpl();
  private final PlaceholderResolver parent;

  public ReflectionResolver(Object value) {
    this(value, new CustomPlaceholderRegistryImpl()); //NoOp CustomPlaceholderRegistry
  }

  public ReflectionResolver(Object value, CustomPlaceholderRegistry customPlaceholderRegistry) {
    this(value, customPlaceholderRegistry, GenerationOptionsBuilder.buildDefaultOptions(), null);
  }

  /**
   * Create a new reflection resolver with a parent registry.
   *
   * @param value                     The value to resolve against
   * @param customPlaceholderRegistry The custom placeholder registry to check for custom placeholders
   * @param options                   Options for generating the document
   * @param parent                    The parent registry
   */
  public ReflectionResolver(Object value,
                            CustomPlaceholderRegistry customPlaceholderRegistry,
                            GenerationOptions options,
                            PlaceholderResolver parent) {
    this.bean = value;
    this.customPlaceholderRegistry = customPlaceholderRegistry;
    this.parent = parent;
    setOptions(options);
  }

  /**
   * Create a new reflection resolver with no parent registry.
   *
   * @param value                     The value to resolve against
   * @param customPlaceholderRegistry The custom placeholder registry to check for custom placeholders
   * @param options                   Options for generating the document
   */
  public ReflectionResolver(Object value,
                            CustomPlaceholderRegistry customPlaceholderRegistry,
                            GenerationOptions options) {
    this(value, customPlaceholderRegistry, options, null);
  }

  private static boolean isFieldAnnotatedWith(Class<?> clazz, String fieldName, Class<? extends Annotation> annotation) {
    try {
      return Arrays.stream(clazz.getDeclaredFields())
          .filter(field -> field.getName().equalsIgnoreCase(fieldName))
          .findFirst()
          .map(field -> field.getDeclaredAnnotation(annotation) != null)
          .orElseGet(() -> {
            logger.debug("Class %s does not have field %s".formatted(clazz, fieldName));
            return false;
          });
    } catch (SecurityException e) {
      logger.warn(e);
      return false;
    }
  }

  private static NumberFormat toNumberFormat(Percentage percentage, Locale locale) {
    var format = NumberFormat.getPercentInstance(locale);
    if (percentage.maxFractionDigits() > -1) {
      format.setMaximumFractionDigits(percentage.maxFractionDigits());
    }
    return format;
  }

  private static NumberFormat toNumberFormat(Money money, Locale locale) {
    var currency = !money.currencyCode().isBlank()
        ? Currency.getInstance(money.currencyCode()) :
        Currency.getInstance(locale);
    var format = NumberFormat.getCurrencyInstance(locale);
    format.setCurrency(currency);
    return format;
  }

  private static NumberFormat toNumberFormat(Numeric numeric, Locale locale) {
    var format = NumberFormat.getNumberInstance(locale);
    if (numeric.maxFractionDigits() != -1) {
      format.setMaximumFractionDigits(numeric.maxFractionDigits());
    }
    if (numeric.minFractionDigits() != -1) {
      format.setMinimumFractionDigits(numeric.minFractionDigits());
    }
    if (numeric.maxIntDigits() != -1) {
      format.setMaximumIntegerDigits(numeric.maxIntDigits());
    }
    if (numeric.minIntDigits() != -1) {
      format.setMinimumIntegerDigits(numeric.minIntDigits());
    }
    if (!numeric.currencyCode().isEmpty()) {
      format.setCurrency(Currency.getInstance(numeric.currencyCode()));
    }
    format.setGroupingUsed(numeric.groupingUsed());
    format.setParseIntegerOnly(numeric.parseIntegerOnly());
    if (numeric.roundingMode() != RoundingMode.UNNECESSARY) {
      format.setRoundingMode(numeric.roundingMode());
    }
    return format;
  }

  private static DateTimeFormatter toDateTimeFormatter(Format format) {
    var formatter = DateTimeFormatter.ofPattern(format.value());
    if (!format.zone().isBlank()) {
      formatter = formatter.withZone(ZoneId.of(format.zone()));
    }
    if (!format.locale().isBlank()) {
      formatter = formatter.withLocale(Locale.forLanguageTag(format.locale()));
    }
    return formatter;
  }

  @Override
  protected Optional<PlaceholderData> doResolve(String placeholderName, Locale locale) {
    logger.debug("Trying to resolve placeholder {}", placeholderName);
    boolean isCondition = placeholderMapper.tryToMap(placeholderName).endsWith("?");
    placeholderName = isCondition ? strip(placeholderName) : placeholderName;
    Optional<PlaceholderData> result = resolveStripped(locale, placeholderName);
    if (isCondition) {
      return evaluateCondition(result);
    }
    return result;
  }

  private String strip(String placeholderName) {
    var placeholder = placeholderMapper.tryToMap(placeholderName);
    return placeholder.substring(0, placeholder.length() - 1);
  }

  private static boolean validateDynamicAccessMethod(Method method) {
    var returnType = method.getReturnType();
    if (!returnType.equals(Optional.class)) {
      if (method.getParameterCount() != 1) {
        if (!method.getParameterTypes()[0].isAssignableFrom(MatchPlaceholderData.class)) {
          logger.warn("@DynamicAccessPlaceholder: parameter should be assignable to MatchPlaceholderData");
          return false;
        }
        logger.warn("@DynamicAccessPlaceholder: method should only expect one MatchPlaceholderData");
        return false;
      }
      logger.warn("@DynamicAccessPlaceholder: method {} must return a java.util.Optional but returns {}.", method, returnType);
      return false;
    }
    return true;
  }

  private Optional<PlaceholderData> resolveStripped(Locale locale, String placeholder) {
    return matchPattern(placeholder, locale)
        .or(() -> dynamicAccess(placeholder, locale))
        .or(() -> resolveFieldAccessor(placeholder, locale))
        .or(() -> tryResolveInParent(placeholder, locale));
  }

  private Optional<PlaceholderData> matchPattern(String placeholderName, Locale locale) {
    return findMatchPlaceholderMethod(placeholderMapper.tryToMap(placeholderName))
        .flatMap(method -> {
          var returnType = method.getReturnType();
          if (returnType.equals(Optional.class)) {
            try {
              if (method.getParameterCount() == 1) {
                if (method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                  logger.warn(
                      "@MatchPlaceholder-annotated method {}: Using the (String), (String, Locale), and (String, GenerationOptions) match "
                          + "methods is deprecated, please migrate you code to using the MatchPlaceholderData record!",
                      method.getName());
                  return evaluateSingleParameterFunction(placeholderName, method);
                } else if (method.getParameterTypes()[0].isAssignableFrom(MatchPlaceholderData.class)) {
                  return evaluateMatchPlaceholderDataFunction(new MatchPlaceholderData(placeholderName, locale, options), method);
                }
                logger.error("@MatchPlaceholder-annotated method {} should take a MatchPlaceholderData argument", method.getName());
                return Optional.empty();
              } else if (method.getParameterCount() == 2) {
                logger.warn(
                    "@MatchPlaceholder-annotated method {}: Using the (String), (String, Locale), and (String, GenerationOptions) match "
                        + "methods is deprecated, please migrate you code to using the MatchPlaceholderData record!",
                    method.getName());
                var additionalParamType = method.getParameterTypes()[1];
                if (additionalParamType.isAssignableFrom(Locale.class)) {
                  return evaluateTwoParameterFunction(placeholderName, locale, Locale.class, method);
                }
                return evaluateTwoParameterFunction(placeholderName, options, GenerationOptions.class, method);
              } else {
                logger.error("@MatchPlaceholder-annotated method {} must take exactly one parameter (MatchPlaceholderData). It takes {}.",
                    method, method.getParameterCount());
                return Optional.empty();
              }
            } catch (IllegalAccessException | InvocationTargetException e) {
              logger.error("@MatchPlaceholder-annotated method %s threw an exception.".formatted(method), e);
              return Optional.empty();
            }
          } else {
            logger.warn("@MatchPlaceholder-annotated method {} must return a java.util.Optional but returns {}.", method, returnType);
            return Optional.empty();
          }
        })
        .map(ScalarPlaceholderData::new);
  }

  private Optional<Method> findMatchPlaceholderMethod(String placeholderName) {
    var beanClass = bean.getClass();
    return Arrays.stream(beanClass.getMethods()).filter(
        method -> Optional.ofNullable(method.getAnnotation(MatchPlaceholder.class)).map(MatchPlaceholder::pattern).filter(placeholderName::matches)
            .isPresent()).findFirst();
  }

  private Optional<PlaceholderData> dynamicAccess(String placeholderName, Locale locale) {
    return findDynamicAccessMethod(placeholderMapper.tryToMap(placeholderName))
        .filter(ReflectionResolver::validateDynamicAccessMethod)
        .flatMap(method -> {
          try {
            var returnValue = method.invoke(bean, new MatchPlaceholderData(placeholderName, locale, options));
            if (returnValue instanceof Optional<?> returnOptional) {
              if (returnOptional.isPresent()) {
                return toPlaceholderData(placeholderName, locale, returnOptional.get());
              } else {
                return Optional.empty();
              }
            } else {
              logger.warn("@DynamicAccessPlaceholder: method {} does not return a java.util.Optional!", method);
              return Optional.empty();
            }
          } catch (InvocationTargetException | IllegalAccessException e) {
            logger.warn(e);
            return Optional.empty();
          }
        });
  }

  private Optional<Method> findDynamicAccessMethod(String placeholderName) {
    var beanClass = bean.getClass();
    return Arrays.stream(beanClass.getMethods()).filter(
        method -> Optional.ofNullable(method.getAnnotation(DynamicAccessPlaceholder.class)).map(DynamicAccessPlaceholder::pattern)
            .filter(placeholderName::matches)
            .isPresent()).findFirst();
  }


  private Optional<String> evaluateSingleParameterFunction(String placeholderName, Method method)
      throws IllegalAccessException, InvocationTargetException {
    var parameterTypes = method.getParameterTypes();
    if (!parameterTypes[0].equals(String.class)) {
      logger.warn("@MatchPlaceholder-annotated method {} must take a String (placeholderName) as first parameter, but takes {}.", method,
          parameterTypes[0]);
      return Optional.empty();
    }
    var returnValue = method.invoke(bean, placeholderName);
    if (returnValue instanceof Optional<?> optionalReturnValue) {
      return optionalReturnValue.map(Object::toString);
    } else {
      logger.warn("@MatchPlaceholder-annotated method {} does not return a java.util.Optional!", method);
      return Optional.empty();
    }
  }

  private <T> Optional<String> evaluateTwoParameterFunction(String placeholderName, T additionalOption, Class<T> additionalOptionClass, Method method)
      throws IllegalAccessException, InvocationTargetException {
    var parameterTypes = method.getParameterTypes();
    if (!parameterTypes[0].equals(String.class)) {
      logger.warn("@MatchPlaceholder-annotated method {} must take a String (placeholderName) as first parameter, but takes {}.", method,
          parameterTypes[0]);
      return Optional.empty();
    }
    if (!parameterTypes[1].equals(additionalOptionClass)) {
      logger.warn("@MatchPlaceholder-annotated method {} can only take a {} as second parameter, but takes {}.", method,
          additionalOptionClass, parameterTypes[1]);
      return Optional.empty();
    }
    var returnValue = method.invoke(bean, placeholderName, additionalOption);
    if (returnValue instanceof Optional<?> optionalReturnValue) {
      return optionalReturnValue.map(Object::toString);
    } else {
      logger.warn("@MatchPlaceholder-annotated method {} must return `Optional<T>`, but returns {}.", method, returnValue.getClass());
      return Optional.empty();
    }
  }

  private Optional<String> evaluateMatchPlaceholderDataFunction(MatchPlaceholderData matchPlaceholderData, Method method)
      throws InvocationTargetException, IllegalAccessException {
    var returnValue = method.invoke(bean, matchPlaceholderData);
    if (returnValue instanceof Optional<?> optionalReturnValue) {
      return optionalReturnValue.map(Object::toString);
    } else {
      logger.warn("@MatchPlaceholder-annotated method {} does not return a java.util.Optional!", method);
      return Optional.empty();
    }
  }

  private Optional<PlaceholderData> resolveFieldAccessor(String placeholderName, Locale locale) {
    return resolveChain(placeholderName, locale)
        .or(() -> placeholderMapper.map(placeholderName)
            .flatMap(mappedPlaceholder -> resolveChain(mappedPlaceholder, locale)));

  }

  private Optional<PlaceholderData> resolveChain(String placeholderName, Locale locale) {
    Optional<PlaceholderData> result = Optional.empty();
    for (String property : placeholderName.split("\\.")) {
      result = result
          .flatMap(placeholderData -> placeholderData.stream().findFirst())
          .flatMap(childResolver -> childResolver.resolve(property, locale))
          .or(() -> doReflectiveResolve(property, locale));
    }
    return result;
  }

  private Optional<PlaceholderData> tryResolveInParent(String placeholderName, Locale locale) {
    return Optional.ofNullable(parent)
        .flatMap(parentResolver -> parentResolver.resolve(placeholderName, locale));
  }

  /**
   * Method resolving placeholders for the reflection resolver.
   *
   * @param placeholderName The name of the placeholder
   * @param locale          The locale to user for localization
   * @return An optional containing `PlaceholderData` if it could be resolved
   */
  private Optional<PlaceholderData> doReflectiveResolve(String placeholderName, Locale locale) {
    try {
      if (PARENT_SYMBOL.equals(placeholderName)) {
        return Optional
            .ofNullable(parent)
            .map(IterablePlaceholderData::of);
      }
      if (customPlaceholderRegistry.governs(placeholderName, bean, options.documentMimeType())) {
        logger.debug("Placeholder {} handled by custom registry", placeholderName);
        return customPlaceholderRegistry.resolve(placeholderName, bean);
      }
      var wrappedProperty = getBeanProperty(placeholderName);
      if (wrappedProperty.isEmpty()) {
        return Optional.of(new ScalarPlaceholderData<>(""));
      }
      return toPlaceholderData(placeholderName, locale, wrappedProperty.get());
    } catch (NoSuchMethodException | IllegalArgumentException e) {
      logger.debug("Did not find placeholder {}, {}", placeholderName, e.getMessage());
      return Optional.empty();
    } catch (IllegalAccessException | InvocationTargetException e) {
      logger.error("Could not call method of placeholder %s".formatted(placeholderName), e);
      return Optional.empty();
    } catch (InstantiationException e) {
      logger.warn("InstantiationException when resolving custom placeholder %s".formatted(placeholderName), e);
      return Optional.empty();
    } catch (ClassCastException e) {
      logger.warn("ClassCastException when resolving custom placeholder %s".formatted(placeholderName), e);
      return Optional.empty();
    }
  }

  private Optional<PlaceholderData> toPlaceholderData(String placeholderName, Locale locale, Object wrappedProperty) {
    try {
      var property = resolveNonFinalValue(wrappedProperty, placeholderName);
      var simplePlaceholder = resolveSimplePlaceholder(property, placeholderName, locale, options);
      if (simplePlaceholder.isPresent()) {
        logger.debug("Placeholder {} resolved to simple placeholder", placeholderName);
        return simplePlaceholder;
      } else if (property instanceof Collection<?> collection) {
        logger.debug("Placeholder {} resolved to collection", placeholderName);
        List<PlaceholderResolver> list = collection.stream()
            // cast is needed for `.toList()`
            .map(object -> (PlaceholderResolver) new ReflectionResolver(object, customPlaceholderRegistry, options, this))
            .toList();
        return Optional.of(new IterablePlaceholderData(list, list.size()));
      } else if (property instanceof PlaceholderData placeholderData) {
        return Optional.of(placeholderData);
      } else if (property instanceof PlaceholderDataFactory placeholderDataFactory) {
        return Optional.of(placeholderDataFactory.create(customPlaceholderRegistry, options, this));
      } else if (bean.equals(property)) {
        logger.debug("Placeholder {} resolved to self", placeholderName);
        return Optional.of(new IterablePlaceholderData(List.of(new ReflectionResolver(bean, customPlaceholderRegistry, options, this)), 1));
      } else {
        return Optional.of(new IterablePlaceholderData(List.of(new ReflectionResolver(property, customPlaceholderRegistry, options, this)), 1));
      }
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

  private Optional<PlaceholderData> resolveSimplePlaceholder(Object property, String placeholderName, Locale locale, GenerationOptions options) {
    if (property == null) {
      return Optional.empty();
    } else if (property instanceof Number number) {
      var numberFormat = findNumberFormat(placeholderName, locale);
      return Optional.of(new ScalarPlaceholderData<>(number, numberFormat::format));
    } else if (property instanceof String propertyString && isFieldAnnotatedWith(bean.getClass(), placeholderName, Translatable.class)) {
      return Optional.of(new ScalarPlaceholderData<>(options.translate(propertyString, locale).orElse(propertyString)));
    } else if (property instanceof Enum<?> enumProperty && isFieldAnnotatedWith(bean.getClass(), placeholderName, Translatable.class)) {
      var translation = options.translate(enumProperty.toString(), locale);
      if (translation.isPresent()) {
        return Optional.of(new ScalarPlaceholderData<>(translation.get()));
      } else {
        return Optional.of(new ScalarPlaceholderData<>(enumProperty));
      }
    } else if (isFieldAnnotatedWith(bean.getClass(), placeholderName, Translatable.class)) {
      return getObjectTranslation(placeholderName, locale, options);
    } else if (property instanceof Enum || property instanceof String || ReflectionUtils.isWrapperType(property.getClass())) {
      return Optional.of(new ScalarPlaceholderData<>(property));
    } else if (property instanceof Temporal temporal) {
      return formatTemporal(placeholderName, temporal, locale);
    } else if (property instanceof Path path && isFieldAnnotatedWith(bean.getClass(), placeholderName, Image.class)) {
      return ReflectionUtils.findFieldAnnotation(bean.getClass(), placeholderName, Image.class)
          .map(image -> new ImagePlaceholderData(path)
              .withMaxWidth(image.maxWidth())
              .withMaxHeight(image.maxHeight())
              .withFileDeletionAfterInsertion(image.deleteAfterInsertion()));
    } else if (property instanceof UUID uuid) {
      return Optional.of(new ScalarPlaceholderData<>(uuid.toString()));
    } else {
      return Optional.empty();
    }
  }

  private Optional<PlaceholderData> getObjectTranslation(String placeholderName, Locale locale, GenerationOptions options) {
    var translatable = ReflectionUtils.findFieldAnnotation(bean.getClass(), placeholderName, Translatable.class);
    if (translatable.isPresent()) {
      try {
        Optional<Object> beanProperty = getBeanProperty(placeholderName);
        if (beanProperty.isPresent()) {
          String toStringMethod = translatable.get().toStringMethod();
          String propertyString = (String) beanProperty.get().getClass().getMethod(toStringMethod).invoke(beanProperty.get());
          return Optional.of(new ScalarPlaceholderData<>(options.translate(propertyString, locale).orElse(propertyString)));
        }
      } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
        logger.error("Could not invoke custom 'toString' method", e);
      }
    }
    return Optional.empty();
  }

  private Optional<Object> getBeanProperty(String placeholderName) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    if (SELF_REFERENCE.equals(placeholderName)) {
      return Optional.ofNullable(bean);
    } else if (bean.getClass().isRecord()) {
      var accessor = Arrays.stream(bean.getClass().getRecordComponents())
          .filter(recordComponent -> recordComponent.getName().equalsIgnoreCase(placeholderName))
          .map(RecordComponent::getAccessor)
          .findFirst()
          .orElseThrow(() -> new NoSuchMethodException("Record %s does not have field %s".formatted(bean.getClass().toString(), placeholderName)));
      return Optional.ofNullable(accessor.invoke(bean));
    } else {
      //This is case-sensitive
      return Optional.ofNullable(pub.getProperty(bean, placeholderName));
    }
  }

  private Optional<PlaceholderData> evaluateCondition(Optional<PlaceholderData> result) {
    if (result.isPresent() && result.get().isTruthy()) {
      return Optional.of(new IterablePlaceholderData(this));
    }
    return Optional.of(new IterablePlaceholderData());
  }

  private Optional<PlaceholderData> formatTemporal(String placeholderName, Temporal time, Locale locale) {
    Optional<DateTimeFormatter> formatter;
    if (isFieldAnnotatedWith(bean.getClass(), placeholderName, Format.class)) {
      formatter = ReflectionUtils.findFieldAnnotation(bean.getClass(), placeholderName, Format.class)
          .map(ReflectionResolver::toDateTimeFormatter);
    } else if (options.tryToFormat(locale, time).isPresent()) {
      return Optional.of(new ScalarPlaceholderData<>(options.tryToFormat(locale, time).get()));
    } else {
      if (time instanceof LocalDate) {
        formatter = Optional.of(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
      } else if (time instanceof LocalTime) {
        formatter = Optional.of(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
      } else if (time instanceof LocalDateTime) {
        formatter = Optional.of(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
      } else {
        logger.warn("Failed to format placeholder {} as temporal {}", placeholderName, time);
        formatter = Optional.empty();
      }
      formatter = formatter.map(dateTimeFormatter -> dateTimeFormatter.withLocale(locale));
    }
    return formatter.map(dateTimeFormatter -> new ScalarPlaceholderData<>(time, dateTimeFormatter::format));
  }

  private NumberFormat findNumberFormat(String fieldName, Locale locale) {
    return ReflectionUtils.findFieldAnnotation(bean.getClass(), fieldName, Percentage.class)
        .map(percentage -> toNumberFormat(percentage, locale))
        .or(() -> ReflectionUtils.findFieldAnnotation(bean.getClass(), fieldName, Money.class)
            .map(money -> toNumberFormat(money, locale)))
        .or(() -> ReflectionUtils.findFieldAnnotation(bean.getClass(), fieldName, Numeric.class)
            .map(numeric -> toNumberFormat(numeric, locale)))
        .orElseGet(() -> {
          logger.debug("Did not find formatting directive for {}, formatting according to locale {}", fieldName, locale);
          return NumberFormat.getInstance(locale);
        });
  }

  private Object resolveNonFinalValue(Object property, String placeholderName)
      throws ExecutionException, InterruptedException, TimeoutException, EmptyOptionalException {
    var resolvedProperty = property;
    if (property instanceof Future<?> future) {
      logger.debug("Placeholder {} property is a future, getting it", placeholderName);
      resolvedProperty = future.get(options.maximumWaitTime().toSeconds(), TimeUnit.SECONDS);
      logger.debug("Placeholder {} property future retrieved", placeholderName);
      return resolveNonFinalValue(resolvedProperty, placeholderName);
    }
    if (property instanceof Optional<?> optional) {
      logger.debug("Placeholder {} property is an optional, getting it", placeholderName);
      if (optional.isEmpty()) {
        throw new EmptyOptionalException(placeholderName);
      } else {
        resolvedProperty = optional.get();
        logger.debug("Optional placeholder {} property contained {}", placeholderName, property);
        return resolveNonFinalValue(resolvedProperty, placeholderName);
      }
    }
    return resolvedProperty;
  }

  @Override
  public String toString() {
    return bean != null ? bean.toString() : "";
  }
}
