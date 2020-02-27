package com.docutools.jocument.impl.word;

import static com.docutools.jocument.impl.DocumentImpl.TAG_PATTERN;

import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.ParsingUtils;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

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
    } else if (element instanceof XWPFParagraph) {
      transform((XWPFParagraph) element);
    } else if (element instanceof XWPFTable) {
      transform((XWPFTable) element);
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
    WordUtilities.replaceText(
            paragraph,
            TAG_PATTERN
                    .matcher(WordUtilities.toString(paragraph))
                    .replaceAll(this::fillPlaceholder)
    );
  }

  private void unrollLoop(XWPFParagraph start, List<IBodyElement> remaining) {
    var placeholderName = extractPlaceholderName(start);
    var placeholderData = resolver.resolve(placeholderName)
            .filter(p -> p.getType() == PlaceholderType.LIST)
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
            .takeWhile(element -> !(element instanceof XWPFParagraph) || !endLoopMarker.equals(WordUtilities.toString((XWPFParagraph) element)))
            .collect(Collectors.toList());
  }

  private boolean isLoopStart(IBodyElement element) {
    return element instanceof XWPFParagraph
            && resolver.resolve(
            ParsingUtils.stripBrackets(
                    WordUtilities.toString((XWPFParagraph) element)
            )).map(PlaceholderData::getType)
            .map(type -> type == PlaceholderType.LIST)
            .orElse(false);
  }

  private boolean isCustomPlaceholder(IBodyElement element) {
    return element instanceof XWPFParagraph
            && resolver.resolve(
            ParsingUtils.stripBrackets(
                    WordUtilities.toString((XWPFParagraph) element).trim()
            )).map(PlaceholderData::getType)
            .map(type -> type == PlaceholderType.CUSTOM)
            .orElse(false);
  }

  private String extractPlaceholderName(XWPFParagraph paragraph) {
    return ParsingUtils.stripBrackets(WordUtilities.toString(paragraph));
  }

  private String fillPlaceholder(MatchResult result) {
    String placeholder = result.group();
    String placeholderName = ParsingUtils.stripBrackets(placeholder);
    return resolver.resolve(placeholderName)
            .map(PlaceholderData::toString)
            .orElse("-");
  }
}
