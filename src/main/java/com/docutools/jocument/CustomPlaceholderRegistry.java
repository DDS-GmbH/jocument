package com.docutools.jocument;

import com.docutools.jocument.impl.word.CustomWordPlaceholderData;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public interface CustomPlaceholderRegistry {
    void addHandler(String placeholder, Class<? extends CustomWordPlaceholderData> customWordPlaceholderDataClass);

    Optional<PlaceholderData> resolve(String placeholder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

    boolean governs(String placeholderName);
}
