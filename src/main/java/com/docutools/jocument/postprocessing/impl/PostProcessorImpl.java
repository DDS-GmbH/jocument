package com.docutools.jocument.postprocessing.impl;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.postprocessing.PostProcessingResolver;
import com.docutools.jocument.postprocessing.PostProcessor;

import java.util.LinkedList;
import java.util.List;

public class PostProcessorImpl<T> implements PostProcessor<T> {
    private final List<PostProcessingResolver<T>> postProcessingResolvers = new LinkedList<>();

    @Override
    public void addPostProcessingResolver(PostProcessingResolver<T> postProcessingResolver) {
        postProcessingResolvers.add(postProcessingResolver);
    }

    @Override
    public void process(T document, PlaceholderResolver resolver) {
        for (PostProcessingResolver<T> postProcessingResolver : postProcessingResolvers) {
            postProcessingResolver.documentGenerationFinished(document, resolver);
        }
    }
}
