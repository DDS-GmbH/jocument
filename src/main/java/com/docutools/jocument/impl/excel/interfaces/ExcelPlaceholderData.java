package com.docutools.jocument.impl.excel.interfaces;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.impl.excel.util.ModificationInformation;
import java.util.Locale;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;

/**
 * An interface which is used by custom placeholders for Excel reports resolving single cells.
 */
public interface ExcelPlaceholderData extends PlaceholderData {
  /**
   * Transformation method for {@link PlaceholderData} needing to insert into excel. Since xlsx generation generates the document newly, as opposed to
   * working on a copy (like word generation) we need the possibility to write to the document for some custom placeholders.
   *
   * @param cell        The cell containing the placeholder
   * @param excelWriter The {@link ExcelWriter} to write to the excel document
   * @param offset      any offset that should be regarded when inserting a new cell (caused by e.g. ranged placeholders in the row)
   * @param locale      the {@link Locale}
   * @param options     the {@link GenerationOptions}
   * @return Modification information specifying the last column number which has been modified by this placeholder (or {@link Optional#empty()} if it
   *     just modified the passed cell) and whether the custom placeholder application resulted in any offsets in the rows columns.
   */
  ModificationInformation transform(Cell cell, ExcelWriter excelWriter, int offset, Locale locale, GenerationOptions options);
}
