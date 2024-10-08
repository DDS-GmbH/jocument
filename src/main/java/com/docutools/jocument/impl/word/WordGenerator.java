package com.docutools.jocument.impl.word;

import static com.docutools.jocument.impl.DocumentImpl.TAG_PATTERN;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.PlaceholderType;
import com.docutools.jocument.impl.ParsingUtils;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;

class WordGenerator {
  private static final Logger logger = LogManager.getLogger();

  private final PlaceholderResolver resolver;
  private final List<IBodyElement> elements;
  private final GenerationOptions options;

  private WordGenerator(PlaceholderResolver resolver, List<IBodyElement> elements, GenerationOptions options) {
    this.resolver = resolver;
    this.elements = elements;
    this.options = options;
  }

  static void apply(PlaceholderResolver resolver, List<IBodyElement> elements, GenerationOptions options) {
    new WordGenerator(resolver, elements, options).generate();
  }

  private void generate() {
    logger.debug("Starting generation by applying resolver {} to elements {}", resolver, elements);
    for (int i = 0; i < elements.size(); i++) {
      var element = elements.get(i);

      if (element instanceof XWPFSDT || !WordUtilities.exists(element)) {
        continue;
      }

      var remaining = elements.subList(i + 1, elements.size());
      transform(element, remaining);
    }
    logger.debug("Finished generation of elements {} by resolver {}", elements, resolver);
  }

  private void transform(IBodyElement element, List<IBodyElement> remaining) {
    logger.debug("Trying to transform element {}", element);
    if (isCustomPlaceholder(element)) {
      resolver.resolve(WordUtilities.extractPlaceholderName((XWPFParagraph) element))
          .ifPresent(placeholderData -> placeholderData.transform(element, LocaleUtil.getUserLocale(), options));
    } else if (isLoopStart(element, remaining)) {
      unrollLoop((XWPFParagraph) element, remaining);
    } else if (element instanceof XWPFParagraph xwpfParagraph) {
      transform(xwpfParagraph);
    } else if (element instanceof XWPFTable xwpfTable) {
      transform(xwpfTable);
    } else {
      logger.info("Failed to transform element {}", element);
    }
  }

  private void transform(XWPFTable table) {
    table.getRows()
        .stream()
        .flatMap(xwpfTableRow -> xwpfTableRow.getTableCells().stream())
        .map(XWPFTableCell::getBodyElements)
        .forEachOrdered(bodyElements -> new WordGenerator(this.resolver, bodyElements, options).generate());
    logger.debug("Transformed table {}", table);
  }

  private void transform(XWPFParagraph paragraph) {
    Matcher matcher = TAG_PATTERN.matcher(WordUtilities.toString(paragraph));
    if (matcher.find()) {
      WordUtilities.replaceText(paragraph, matcher.replaceAll(matchResult -> fillPlaceholder(matchResult, LocaleUtil.getUserLocale())));
    }
    logger.debug("Transformed paragraph {}", paragraph);
  }

  private void unrollLoop(XWPFParagraph start, List<IBodyElement> remaining) {
    var placeholderName = WordUtilities.extractPlaceholderName(start);
    logger.debug("Unrolling loop of {}", placeholderName);
    var placeholderData = resolver.resolve(placeholderName)
        .filter(p -> p.getType() == PlaceholderType.SET)
        .orElseThrow();
    var content = getLoopBody(placeholderName, remaining);

    placeholderData.stream().forEach(itemResolver ->
        apply(itemResolver, WordUtilities.copyBefore(content, start), options));

    removeLoop(start, content, remaining);
    logger.debug("Unrolled loop of {}", placeholderName);
  }

  private void removeLoop(IBodyElement start, List<IBodyElement> content, List<IBodyElement> remaining) {
    WordUtilities.removeIfExists(start);
    content.forEach(WordUtilities::removeIfExists);
    WordUtilities.removeIfExists(remaining.get(content.size()));
  }

  private List<IBodyElement> getLoopBody(String placeholderName, List<IBodyElement> remaining) {
    var endLoopMarkers = ParsingUtils.getMatchingLoopEnds(placeholderName);
    logger.debug("Getting loop body from {}", placeholderName);
    return remaining.stream()
        //Could be written nice with `takeUntil(element -> (element instanceof XP xp && eLM.equals(WU.toString(xp)))
        .takeWhile(element -> !(element instanceof XWPFParagraph xwpfParagraph
            && endLoopMarkers.stream().anyMatch(endLoopMarker -> endLoopMarker.equals(WordUtilities.toString(xwpfParagraph).strip().toLowerCase()))))
        .toList();
  }

  private boolean isLoopStart(IBodyElement element, List<IBodyElement> remaining) {
    if (element instanceof XWPFParagraph xwpfParagraph) {
      Matcher matcher = TAG_PATTERN.matcher(WordUtilities.toString(xwpfParagraph));
      if (matcher.find()) {
        var placeholderName = matcher.group(1);
        return resolver.resolve(placeholderName)
            .filter(placeholderData -> placeholderData.getType() == PlaceholderType.SET)
            .map(placeholderData -> {
              var endLoopMarkers = ParsingUtils.getMatchingLoopEnds(placeholderName);
              return remaining.stream()
                  .filter(XWPFParagraph.class::isInstance)
                  .map(XWPFParagraph.class::cast)
                  .map(XWPFParagraph::getText)
                  .anyMatch(text -> endLoopMarkers.contains(text.strip().toLowerCase()));
            }).orElse(false);
      }
    }
    return false;
  }

  private boolean isCustomPlaceholder(IBodyElement element) {
    if (!(element instanceof XWPFParagraph xwpfParagraph)) {
      return false;
    }
    String paragraphText = WordUtilities.toString(xwpfParagraph);
    if (!ParsingUtils.containsPlaceholder(paragraphText)) {
      return false;
    }
    return resolver.resolve(
    ParsingUtils.stripBrackets(
        paragraphText
    )).map(PlaceholderData::getType)
    .map(type -> type == PlaceholderType.CUSTOM)
    .orElse(false);
  }

  private String fillPlaceholder(MatchResult result, Locale locale) {
    String placeholder = result.group();
    String placeholderName = ParsingUtils.stripBrackets(placeholder);
    logger.debug("Resolving placeholder {}", placeholderName);
    return resolver.resolve(placeholderName, locale)
        .map(PlaceholderData::toString)
        .orElse("");
  }
}
