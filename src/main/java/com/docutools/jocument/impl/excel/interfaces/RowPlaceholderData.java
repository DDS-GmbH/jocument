package com.docutools.jocument.impl.excel.interfaces;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Row;

public interface RowPlaceholderData extends PlaceholderData {
  /**
   * Transformation method for {@link PlaceholderData} needing to insert into excel. Since xlsx generation generates the document newly, as opposed to
   * working on a copy (like word generation) we need the possibility to write to the document for some custom placeholders.
   *
   * @param row         The row containing the placeholder
   * @param excelWriter The {@link ExcelWriter} to write to the excel document
   * @param locale      the {@link Locale}
   * @param options     the {@link GenerationOptions}
   */
  default void transform(Row row, ExcelWriter excelWriter, Locale locale, GenerationOptions options) {
    throw new UnsupportedOperationException();
  }
}
