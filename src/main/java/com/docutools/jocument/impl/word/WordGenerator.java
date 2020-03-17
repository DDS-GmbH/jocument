package com.docutools.jocument.impl.word;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.ParsingUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

import static com.docutools.jocument.impl.DocumentImpl.TAG_PATTERN;

class WordGenerator {

  private final PlaceholderResolver resolver;
  private final List<IBodyElement> elements;

  private WordGenerator(PlaceholderResolver resolver, List<IBodyElement> elements) {
    this.resolver = resolver;
    this.elements = elements;
  }

  static void apply(PlaceholderResolver resolver, List<IBodyElement> elements) {
    new WordGenerator(resolver, elements).generate();
  }

  private void generate() {
    for (int i = 0; i < elements.size(); i++) {
      var element = elements.get(i);

      if (!WordUtilities.exists(element)) {
        continue;
      }

      var remaining = elements.subList(i + 1, elements.size());
      transform(element, remaining);
    }
  }

  private void transform(IBodyElement element, List<IBodyElement> remaining) {
    if (isLoopStart(element)) {
      unrollLoop((XWPFParagraph) element, remaining);
    } else if (isCustomPlaceholder(element)) {
      resolver.resolve(extractPlaceholderName((XWPFParagraph) element))
              .ifPresent(placeholderData -> placeholderData.transform(element));
    } else if (element instanceof XWPFParagraph xwpfParagraph) {
      transform(xwpfParagraph);
    } else if (element instanceof XWPFTable xwpfTable) {
      transform(xwpfTable);
    }
  }

  private void transform(XWPFTable table) {
    table.getRows()
            .stream()
            .map(XWPFTableRow::getTableCells)
            .flatMap(List::stream)
            .map(XWPFTableCell::getParagraphs)
            .flatMap(List::stream)
            .forEach(this::transform);
  }

  private void transform(XWPFParagraph paragraph) {
    Locale locale = WordUtilities.detectMostCommonLocale(paragraph)
            .orElse(LocaleUtil.getUserLocale());
    WordUtilities.replaceText(
            paragraph,
            TAG_PATTERN
                    .matcher(WordUtilities.toString(paragraph))
                    .replaceAll(matchResult -> fillPlaceholder(matchResult, locale))
    );
  }

  private void unrollLoop(XWPFParagraph start, List<IBodyElement> remaining) {
    var placeholderName = extractPlaceholderName(start);
    var placeholderData = resolver.resolve(placeholderName)
            .filter(p -> p.getType() == PlaceholderType.SET)
            .orElseThrow();
    var content = getLoopBody(placeholderName, remaining);

    placeholderData.stream().forEach(itemResolver ->
            apply(itemResolver, WordUtilities.copyBefore(content, start)));

    removeLoop(start, content, remaining);
  }

  private void removeLoop(IBodyElement start, List<IBodyElement> content, List<IBodyElement> remaining) {
    WordUtilities.removeIfExists(start);
    content.forEach(WordUtilities::removeIfExists);
    WordUtilities.removeIfExists(remaining.get(content.size()));
  }

  private List<IBodyElement> getLoopBody(String placeholderName, List<IBodyElement> remaining) {
    var endLoopMarker = String.format("{{/%s}}", placeholderName);
    return remaining.stream()
            //Could be written nice with `takeUntil(element -> (element instanceof XP xp && eLM.equals(WU.toString(xp)))
            .takeWhile(element -> !(element instanceof XWPFParagraph xwpfParagraph &&
                                  endLoopMarker.equals(WordUtilities.toString(xwpfParagraph))))
            .collect(Collectors.toList());
  }

  private boolean isLoopStart(IBodyElement element) {
    return element instanceof XWPFParagraph xwpfParagraph
            && resolver.resolve(
            ParsingUtils.stripBrackets(
                    WordUtilities.toString(xwpfParagraph)
            )).map(PlaceholderData::getType)
            .map(type -> type == PlaceholderType.SET)
            .orElse(false);
  }

  private boolean isCustomPlaceholder(IBodyElement element) {
    return element instanceof XWPFParagraph xwpfParagraph
            && resolver.resolve(
            ParsingUtils.stripBrackets(
                    WordUtilities.toString(xwpfParagraph).trim()
            )).map(PlaceholderData::getType)
            .map(type -> type == PlaceholderType.CUSTOM)
            .orElse(false);
  }

  private String extractPlaceholderName(XWPFParagraph paragraph) {
    return ParsingUtils.stripBrackets(WordUtilities.toString(paragraph));
  }

  private String fillPlaceholder(MatchResult result, Locale locale) {
    String placeholder = result.group();
    String placeholderName = ParsingUtils.stripBrackets(placeholder);
    return resolver.resolve(placeholderName, locale)
            .map(PlaceholderData::toString)
            .orElse("-");
  }
}
