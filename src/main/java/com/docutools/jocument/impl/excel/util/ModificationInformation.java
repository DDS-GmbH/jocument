package com.docutools.jocument.impl.excel.util;

import java.util.Optional;

public record ModificationInformation(Optional<Integer> skipUntil, int offset) {

  public static ModificationInformation empty() {
    return new ModificationInformation(Optional.empty(), 0);
  }

  public ModificationInformation merge(ModificationInformation newModificationInformation) {
    return new ModificationInformation(newModificationInformation.skipUntil(), offset + newModificationInformation.offset());
  }
}
