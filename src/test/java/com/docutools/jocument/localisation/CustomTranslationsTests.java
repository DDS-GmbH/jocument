package com.docutools.jocument.localisation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.PlaceholderData;
import com.docutools.jocument.PlaceholderResolver;
import com.docutools.jocument.impl.CustomPlaceholderRegistryImpl;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import com.docutools.jocument.impl.word.WordUtilities;
import com.docutools.jocument.sample.model.SampleModelData;
import java.util.Locale;
import java.util.Optional;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("Custom translations")
@Tag("automated")
class CustomTranslationsTests {

  @Test
  @DisplayName("Add custom translation function passed to to CustomPlaceholders.")
  void doCustomTranslations() {
    // Arrange
    var registry = new CustomPlaceholderRegistryImpl();
    registry.addHandler("test", CustomTranslatedPlaceholder.class);
    var options = new GenerationOptionsBuilder()
        .withTranslation((term, locale) -> {
          if (locale.equals(Locale.GERMAN)) {
            return Optional.of(term.replace("a", "b"));
          }
          return Optional.of("ccc");
        })
        .build();
    var resolver = new ReflectionResolver(new Object(), registry, options, null);
    var document = new XWPFDocument();
    var paragraph = document.createParagraph();
    paragraph.createRun().setText("aaa");

    // Act
    var actual = resolver.resolve("test", Locale.GERMAN)
        .orElseThrow();

    // Assert
    assertThat(actual, instanceOf(CustomWordPlaceholderData.class));
    if (actual instanceof CustomWordPlaceholderData customPlaceholder) {
      customPlaceholder.transform(paragraph, Locale.GERMAN, options);
      assertThat(paragraph.getText(), equalTo("bbb"));
    }
  }

  @Test
  void translatesAnnotatedTerm() {
    // Arrange
    var options = new GenerationOptionsBuilder()
        .withTranslation((term, locale) -> {
          if (term.equals("sympathetic") && locale.equals(Locale.GERMAN)) {
            return Optional.of("Einfühlend");
          }
          return Optional.empty();
        })
        .build();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD, new CustomPlaceholderRegistryImpl(), options, null);

    // Act
    Optional<PlaceholderData> commandingStyle = resolver.resolve("commandingStyle", Locale.GERMAN);

    // Assert
    assertThat(commandingStyle.get().getRawValue(), equalTo("Einfühlend"));
  }

  public static class CustomTranslatedPlaceholder extends CustomWordPlaceholderData {

    public CustomTranslatedPlaceholder() {
    }

    @Override
    protected void transform(IBodyElement placeholder, XWPFDocument document, Locale locale, GenerationOptions options) {
      if (placeholder instanceof XWPFParagraph paragraph) {
        var translatedText = options.translate(paragraph.getText(), locale).orElse("");
        WordUtilities.replaceText(paragraph, translatedText);
      }
    }

  }
}
