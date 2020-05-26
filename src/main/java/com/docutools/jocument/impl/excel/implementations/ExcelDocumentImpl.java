package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import com.docutools.jocument.impl.excel.util.ExcelUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;


/**
 * This class is responsible for setting up the necessary streams for generating excel document from templates
 * and for passing each sheet of a workbook to the excel generator.
 *
 * @author Anton Oellerer
 * @since 2020-04
 * @version 1.1.0
 */
public class ExcelDocumentImpl extends DocumentImpl {


    /**
     * The constructor for a new ExcelDocument report generator
     * @param template The template to generate the report from
     * @param resolver The resolver to use for filling placeholders
     */
    public ExcelDocumentImpl(Template template, PlaceholderResolver resolver) {
        super(template, resolver);
    }

    /**
     * Start generation of a excel report from the template supplied in the constructor, using the also supplied
     * resolver for resolving placeholders.
     * @return The path to the generated report
     * @throws IOException If reading of the template or writing of the report fails.
     */
    @Override
    protected Path generate() throws IOException {
        Path file = Files.createTempFile("document", ".xlsx");
        ExcelWriter excelWriter = new SXSSFWriter(file);
        try (XSSFWorkbook workbook = new XSSFWorkbook(template.openStream())) {
            var locale = ExcelUtils.getWorkbookLanguage(workbook).orElse(Locale.getDefault());
            LocaleUtil.setUserLocale(locale);
            for (Iterator<Sheet> it = workbook.sheetIterator(); it.hasNext(); ) {
                Sheet sheet = it.next();
                excelWriter.newSheet(sheet);
                ExcelGenerator.apply(resolver, sheet.rowIterator(), excelWriter);
            }
            excelWriter.complete();
        }
        return file;
    }
}
