package com.docutools.jocument;

import java.util.Map;
import java.util.stream.Stream;

public record MapPlaceholderData(Map<String, PlaceholderData> map) implements PlaceholderData {

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.MAP;
  }

  @Override
  public Stream<PlaceholderResolver> stream() {
    return map.values().stream().filter(placeholderData -> placeholderData.getType().equals(PlaceholderType.SET)).flatMap(PlaceholderData::stream);
  }

  @Override
  public long count() {
    return map.size();
  }

  @Override
  public Object getRawValue() {
    return map;
  }
}
