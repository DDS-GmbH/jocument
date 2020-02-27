package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.annotations.Image;
import com.docutools.jocument.impl.word.placeholders.ImagePlaceholderData;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
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
  private final BeanUtilsBean bub = new BeanUtilsBean();

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
      var type = pub.getPropertyType(bean, placeholderName);
      if (type == null) {
        return Optional.empty();
      }
      if (type.isPrimitive() || type.equals(String.class) || type.isEnum()) {
        return Optional.of(new ScalarPlaceholderData(bub.getProperty(bean, placeholderName)));
      } else if (LocalDate.class.isAssignableFrom(type)) {
        LocalDate date = (LocalDate) pub.getProperty(bean, placeholderName);
        return Optional.of(new ScalarPlaceholderData(date.format(DateTimeFormatter.ISO_DATE)));
      } else if (Collection.class.isAssignableFrom(type)) {
        Collection<Object> property = (Collection<Object>) pub.getProperty(bean, placeholderName);
        List<PlaceholderResolver> list = property.stream()
                .map(ReflectionResolver::new)
                .collect(Collectors.toList());
        return Optional.of(new IterablePlaceholderData(list, list.size()));
      } else if (Path.class.isAssignableFrom(type) && isFieldAnnotatedWith(bean.getClass(), placeholderName, Image.class)) {
        return Optional.of(new ImagePlaceholderData((Path) pub.getProperty(bean, placeholderName)));
      } else {
        var value = pub.getProperty(bean, placeholderName);
        return Optional.of(new IterablePlaceholderData(List.of(new ReflectionResolver(value)), 1));
      }
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new IllegalStateException("Could not resolve placeholderName against type.", e);
    }
  }
}
