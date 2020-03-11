package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.annotations.Format;
import com.docutools.jocument.annotations.Image;
import com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * Takes a {@link java.lang.Object} of any type and resolves placeholder names with reflective access to its type.
 *
 * @author codecitizen
 * @see com.docutools.jocument.PlaceholderResolver
 * @since 1.0-SNAPSHOT
 */
public class ReflectionResolver implements PlaceholderResolver {

  private final Object bean;
  private final PropertyUtilsBean pub = new PropertyUtilsBean();

  public ReflectionResolver(Object value) {
    this.bean = value;
  }

  private static boolean isFieldAnnotatedWith(Class<?> clazz, String fieldName, Class<? extends Annotation> annotation) {
    try {
      return clazz.getDeclaredField(fieldName)
              .getDeclaredAnnotation(annotation) != null;
    } catch (Exception ignored) {
      return false;
    }
  }

  @Override
  public Optional<PlaceholderData> resolve(String placeholderName) {
    try {
      var property = pub.getProperty(bean, placeholderName);
      if (property instanceof Enum || property instanceof String || property.getClass().isPrimitive()) {
        return Optional.of(new ScalarPlaceholderData(property.toString()));
      } else if (property instanceof Collection<?> collection) {
        List<PlaceholderResolver> list = collection.stream()
                .map(ReflectionResolver::new)
                .collect(Collectors.toList());
        return Optional.of(new IterablePlaceholderData(list, list.size()));
      } else if (property instanceof Temporal time && isFieldAnnotatedWith(bean.getClass(), placeholderName, Format.class)) {
        return ReflectionUtils.findFieldAnnotation(bean.getClass(), placeholderName, Format.class)
                .map(ReflectionResolver::toDateTimeFormatter)
                .map(formatter -> formatter.format(time))
                .map(ScalarPlaceholderData::new);
      } else if (property instanceof Path && isFieldAnnotatedWith(bean.getClass(), placeholderName, Image.class)) {
        return Optional.of(new ImagePlaceholderData((Path) pub.getProperty(bean, placeholderName)));
      } else {
        var value = pub.getProperty(bean, placeholderName);
        return Optional.of(new IterablePlaceholderData(List.of(new ReflectionResolver(value)), 1));
      }
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new IllegalStateException("Could not resolve placeholderName against type.", e);
    }
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
}
