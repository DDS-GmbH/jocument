package com.docutools.jocument.postprocessing;

import com.docutools.jocument.PlaceholderResolver;

public interface PostProcessor<T> {
    void documentGenerationFinished(T document, PlaceholderResolver resolver);
}
