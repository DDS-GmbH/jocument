package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderType;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ScalarPlaceholderData<T> implements PlaceholderData {

  private final T value;
  private final Function<T, String> stringifier;

  public ScalarPlaceholderData(T value) {
    this(value, v -> Objects.toString(v, ""));
  }

  public ScalarPlaceholderData(T value, Function<T, String> stringifier) {
    this.value = value;
    this.stringifier = stringifier;
  }

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.SCALAR;
  }

  @Override
  public String toString() {
    String string = stringifier.apply(value);
    if (string == null || string.isEmpty()) {
      return "-";
    }
    return string;
  }

  @Override
  public Object getRawValue() {
    return value;
  }

  @Override
  public boolean isTruthy() {
    // can be replaced with Java pattern matching when out of preview
    if (value instanceof Number number) {
      return number.longValue() != 0L;
    } else if (value instanceof String string) {
      return !string.isEmpty() && !string.equals("-");
    } else if (value instanceof Boolean bool) {
      return bool;
    } else if (value instanceof Collection<?> collection) {
      return !collection.isEmpty();
    } else if (value instanceof Object[] array) {
      return array.length > 0;
    } else if (value instanceof Optional<?> optional) {
      return optional.isPresent();
    }
    return value != null;
  }
}
