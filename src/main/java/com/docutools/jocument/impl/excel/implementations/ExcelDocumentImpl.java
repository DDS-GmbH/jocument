package com.docutools.jocument.impl.excel.implementations;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.Template;
import com.docutools.jocument.impl.DocumentImpl;
import com.docutools.jocument.impl.excel.interfaces.ExcelWriter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;

public class ExcelDocumentImpl extends DocumentImpl {
    public ExcelDocumentImpl(Template template, PlaceholderResolver resolver) {
        super(template, resolver);
    }

    @Override
    protected Path generate() throws IOException {
        Path file = Files.createTempFile("document", ".xlsx");
        ExcelWriter excelWriter = new SXSSFWriter(file);
        try (XSSFWorkbook workbook = new XSSFWorkbook(template.openStream())) {
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
