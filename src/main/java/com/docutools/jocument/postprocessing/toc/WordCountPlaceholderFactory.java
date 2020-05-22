package com.docutools.jocument.postprocessing.toc;

import com.docutools.jocument.postprocessing.PostProcessingResolver;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class WordCountPlaceholderFactory {
    public static <T> PostProcessingResolver<T> createTableOfContentsPlaceholder(Class<T> documentType) {
        if (documentType == XWPFDocument.class) {
            return (PostProcessingResolver<T>) new WordCountPlaceholderResolver();
        } else {
            throw new UnsupportedOperationException("TOC is only implemented for Word");
        }
    }
}
