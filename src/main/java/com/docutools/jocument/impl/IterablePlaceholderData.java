package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IterablePlaceholderData implements PlaceholderData {

  public IterablePlaceholderData(PlaceholderResolver resolver, int size) {
    this.iterable = List.of(resolver);
    this.count = size;
  }

  public static IterablePlaceholderData of(PlaceholderResolver... resolvers) {
    return new IterablePlaceholderData(Arrays.asList(resolvers), resolvers.length);
  }

  private final Iterable<PlaceholderResolver> iterable;
  private final long count;

  public IterablePlaceholderData() {
    this(List.of(), 0L);
  }

  public IterablePlaceholderData(PlaceholderResolver resolver) {
    this(List.of(resolver), 1L);
  }

  public IterablePlaceholderData(Iterable<PlaceholderResolver> iterable, long count) {
    this.iterable = iterable;
    this.count = count;
  }

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.SET;
  }

  @Override
  public Stream<PlaceholderResolver> stream() {
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  @Override
  public long count() {
    return count;
  }

}
