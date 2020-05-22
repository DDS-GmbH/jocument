package com.docutools.jocument.postprocessing.toc;

import com.docutools.jocument.postprocessing.PostProcessor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class WordCountPlaceholderFactory {
    public static <T> PostProcessor<T> createTableOfContentsPlaceholder(Class<T> documentType) {
        if (documentType == XWPFDocument.class) {
            return (PostProcessor<T>) new WordCountPlaceholderResolver();
        } else {
            throw new UnsupportedOperationException("TOC is only implemented for Word");
        }
    }
}
