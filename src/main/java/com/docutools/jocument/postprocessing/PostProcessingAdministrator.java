package com.docutools.jocument.postprocessing;

import com.docutools.jocument.PlaceholderResolver;

public interface PostProcessingAdministrator<T> {
    void addPostProcessingResolver(PostProcessor<T> postProcessor);

    void process(T document, PlaceholderResolver resolver);
}
