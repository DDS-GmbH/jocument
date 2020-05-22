package com.docutools.jocument.postprocessing.toc;

import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.word.WordUtilities;
import com.docutools.jocument.postprocessing.PostProcessingResolver;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class WordCountPlaceholderResolver implements PostProcessingResolver<XWPFDocument> {
    @Override
    public void documentGenerationFinished(XWPFDocument document, PlaceholderResolver resolver) {
        var countedWords = WordUtilities.getAllParagraphs(document)
                .map(WordUtilities::toString)
                .flatMap(paragraph -> Arrays.stream(paragraph.split("[\\d\\s.!?,;:()\\[\\]{}\\-_\"`~#&*%$\\\\/“”]")))
                .filter(token -> token.length() > 1 && !token.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toMap(word -> word, word -> 1, Integer::sum))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));


        WordUtilities.getAllParagraphsMatchingPlaceholder("wordCount", document)
                .forEach(xwpfParagraph -> {
                    var runs = xwpfParagraph.getRuns();
                    for (int i = runs.size() - 1; i >= 0; i--) {
                        xwpfParagraph.removeRun(0);
                    }
                    var run = xwpfParagraph.createRun();
                    countedWords.forEach(stringIntegerEntry -> {
                        run.setText(stringIntegerEntry.getKey() + ": " + stringIntegerEntry.getValue());
                        run.addBreak();
                    });
                });
    }
}
