package com.docutools.jocument.impl;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IterablePlaceholderData implements PlaceholderData {

  private final Iterable<PlaceholderResolver> iterable;
  private final long count;

  public IterablePlaceholderData(Iterable<PlaceholderResolver> iterable, long count) {
    this.iterable = iterable;
    this.count = count;
  }

  @Override
  public PlaceholderType getType() {
    return PlaceholderType.LIST;
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
