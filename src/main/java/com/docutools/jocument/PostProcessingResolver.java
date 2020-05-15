package com.docutools.jocument;

public interface PostProcessingResolver<T> {
    void documentGenerationFinished(T document, PlaceholderResolver resolver);
}
