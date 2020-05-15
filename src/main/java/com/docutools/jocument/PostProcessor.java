package com.docutools.jocument;

public interface PostProcessor<T> {
    void addPostProcessingResolver(PostProcessingResolver<T> postProcessingResolver);

    void process(T document, PlaceholderResolver resolver);
}
