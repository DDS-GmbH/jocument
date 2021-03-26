package com.docutools.jocument;

import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@DisplayName("Template Loading")
public class TemplateLoading {

    @Test
    @DisplayName("Load word templates from classpath.")
    void shouldLoadWordTemplateFromCP() {
        // Act
        Template template = Template.fromClassPath("/templates/word/UserProfileTemplate.docx")
                .orElseThrow();

        // Assert
        assertThat(template.getMimeType(), is(MimeType.DOCX));
    }

    @Test
    @DisplayName("Return empty value when given classpath resource does not exist.")
    void shouldReturnEmptyWhenCpNotExists() {
        // Act
        var result = Template.fromClassPath("/does/not/exist.docx");

        // Assert
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("Load word templates from the file system.")
    void shouldLoadWordTemplatesFromFs() throws IOException {
        // Arrange
        Path path = null;
        try {
            path = Files.createTempFile("jocument", ".docx");
            try(var in = getClass().getResourceAsStream("/templates/word/UserProfileTemplate.docx")) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }

            var result = Template.from(path);
            assertThat(result.isEmpty(), is(false));
        } finally {
            if(path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Test
    @DisplayName("Template should assume systems default Locale, if none is passed with resource.")
    void shouldAssumeDefaultLocale() {
        // Act
        var result = Template.fromClassPath("/templates/word/UserProfileTemplate.docx")
                .orElseThrow();

        // Assert
        assertThat(result.getLocale(), equalTo(LocaleUtil.getUserLocale()));
    }

    @Test
    @DisplayName("Load Template from Byte Array")
    void shouldLoadTemplateFromByteArray() throws IOException {
        // Arrange
        byte[] data;
        try(var in = getClass().getResourceAsStream("/templates/word/UserProfileTemplate.docx")) {
            try(var out = new ByteArrayOutputStream()) {
                in.transferTo(out);
                data = out.toByteArray();
            }
        }

        // Act
        assertThat(Template.from(data, MimeType.DOCX).isPresent(), is(true));
    }

    @Test
    @DisplayName("Load Template from InputStream")
    void shouldLoadTemplateFromInputStream() throws IOException {
        // Arrange + Act
        try(var in = getClass().getResourceAsStream("/templates/word/UserProfileTemplate.docx")) {
            assertThat(Template.from(in, MimeType.DOCX).isPresent(), is(true));
        }
    }
}
