package com.docutools.jocument.localisation;

import com.docutools.jocument.GenerationOptions;
import com.docutools.jocument.GenerationOptionsBuilder;
import com.docutools.jocument.impl.CustomPlaceholderRegistryImpl;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.impl.word.CustomWordPlaceholderData;
import com.docutools.jocument.impl.word.WordUtilities;
import java.util.Locale;
import java.util.Optional;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@DisplayName("Custom translations")
public class CustomTranslations {

  public static class CustomTranslatedPlaceholder extends CustomWordPlaceholderData {

    public CustomTranslatedPlaceholder() {
    }

    @Override
    protected void transform(IBodyElement placeholder, XWPFDocument document, Locale locale, GenerationOptions options) {
      if(placeholder instanceof XWPFParagraph paragraph) {
        var translatedText = options.translate(paragraph.getText(), locale).orElse("");
        WordUtilities.replaceText(paragraph, translatedText);
      }
    }

  }

  @Test
  @DisplayName("Add custom translation function passed to to CustomPlaceholders.")
  void doCustomTranslations() {
    // Arrange
    var registry = new CustomPlaceholderRegistryImpl();
    registry.addHandler("test", CustomTranslatedPlaceholder.class);
    var options = new GenerationOptionsBuilder()
        .withTranslation((term, locale) -> {
          if(locale.equals(Locale.GERMAN)) {
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
    if(actual instanceof CustomWordPlaceholderData customPlaceholder) {
      customPlaceholder.transform(paragraph, Locale.GERMAN, options);
      assertThat(paragraph.getText(), equalTo("bbb"));
    }
  }

}
