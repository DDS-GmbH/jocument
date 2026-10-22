package com.docutools.jocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.docutools.jocument.image.DefaultImageReference;
import com.docutools.jocument.image.NoWriterFoundException;
import com.docutools.jocument.impl.ReflectionResolver;
import com.docutools.jocument.sample.model.SampleModelData;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Handling temporary files")
@Tag("automated")
class TempFileHandling {

  @TempDir
  Path tempDirectory;

  @ParameterizedTest
  @ValueSource(strings = {"/templates/word/UserProfileTemplate.docx", "/templates/excel/UserProfileTemplate.xlsx"})
  @DisplayName("Write the generated document to the configured temp directory.")
  void shouldWriteDocumentToTempDirectory(String templatePath) throws InterruptedException {
    // Arrange
    Template template = Template.fromClassPath(templatePath).orElseThrow();
    PlaceholderResolver resolver = new ReflectionResolver(SampleModelData.PICARD_PERSON);
    GenerationOptions options = new GenerationOptionsBuilder().withTempDirectory(tempDirectory).build();

    // Act
    Document document = template.startGeneration(resolver, options);
    document.blockUntilCompletion(60000L);

    // Assert
    assertThat(document.getPath(), notNullValue());
    assertThat(document.getPath().getParent(), equalTo(tempDirectory));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/templates/word/UserProfileTemplate.docx", "/templates/excel/UserProfileTemplate.xlsx"})
  @DisplayName("Delete the generated document when generation fails.")
  void shouldDeleteDocumentWhenGenerationFails(String templatePath) throws InterruptedException, IOException {
    // Arrange
    Template template = Template.fromClassPath(templatePath).orElseThrow();
    PlaceholderResolver resolver = new FailingResolver();
    GenerationOptions options = new GenerationOptionsBuilder().withTempDirectory(tempDirectory).build();

    // Act
    Document document = template.startGeneration(resolver, options);
    document.blockUntilCompletion(60000L);

    // Assert
    assertThat(document.getPath(), nullValue());
    assertThat(listFiles(tempDirectory), is(empty()));
  }

  @Test
  @DisplayName("Save images to the given directory.")
  void shouldSaveImageToDirectory() throws IOException, NoWriterFoundException {
    // Arrange
    try (var image = new DefaultImageReference(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB))) {
      // Act
      Path jpeg = image.saveAsJpeg(tempDirectory);
      Path png = image.saveAsPng(tempDirectory);

      // Assert
      assertThat(jpeg.getParent(), equalTo(tempDirectory));
      assertThat(png.getParent(), equalTo(tempDirectory));
    }
  }

  @Test
  @DisplayName("Delete the image file when no writer is found.")
  void shouldDeleteImageWhenNoWriterIsFound() throws IOException {
    // Arrange: JPEG has no writer for images with an alpha channel
    try (var image = new DefaultImageReference(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB))) {
      // Act
      assertThrows(NoWriterFoundException.class, () -> image.saveAsJpeg(tempDirectory));

      // Assert
      assertThat(listFiles(tempDirectory), is(empty()));
    }
  }

  private static List<Path> listFiles(Path directory) throws IOException {
    try (Stream<Path> files = Files.list(directory)) {
      return files.toList();
    }
  }

  private static class FailingResolver extends PlaceholderResolver {

    @Override
    protected Optional<PlaceholderData> doResolve(String placeholderName, Locale locale) {
      throw new IllegalStateException("Resolving %s failed".formatted(placeholderName));
    }

    @Override
    public String toString() {
      return "FailingResolver";
    }
  }
}
