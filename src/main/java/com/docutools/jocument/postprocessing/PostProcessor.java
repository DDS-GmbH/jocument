package com.docutools.jocument.postprocessing;

import com.docutools.jocument.PlaceholderResolver;

public interface PostProcessor<T> {
    void addPostProcessingResolver(PostProcessingResolver<T> postProcessingResolver);

    void process(T document, PlaceholderResolver resolver);
}
