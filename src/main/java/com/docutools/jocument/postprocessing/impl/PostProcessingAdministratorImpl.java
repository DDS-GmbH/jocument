package com.docutools.jocument.postprocessing.impl;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.postprocessing.PostProcessor;
import com.docutools.jocument.postprocessing.PostProcessingAdministrator;

import java.util.LinkedList;
import java.util.List;

public class PostProcessingAdministratorImpl<T> implements PostProcessingAdministrator<T> {
    private final List<PostProcessor<T>> postProcessors = new LinkedList<>();

    @Override
    public void addPostProcessingResolver(PostProcessor<T> postProcessor) {
        postProcessors.add(postProcessor);
    }

    @Override
    public void process(T document, PlaceholderResolver resolver) {
        for (PostProcessor<T> postProcessor : postProcessors) {
            postProcessor.documentGenerationFinished(document, resolver);
        }
    }
}
