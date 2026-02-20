package com.github.scribejava.core.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamUtilsTest {

    @Test
    public void shouldGetStreamContents() throws IOException {
        final String contents = "hello world";
        final InputStream is = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
        assertThat(StreamUtils.getStreamContents(is)).isEqualTo(contents);
    }

    @Test
    public void shouldGetGzipStreamContents() throws IOException {
        final String contents = "hello world gzip";
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(contents.getBytes(StandardCharsets.UTF_8));
        }
        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        assertThat(StreamUtils.getGzipStreamContents(is)).isEqualTo(contents);
    }

    @Test
    public void shouldThrowExceptionOnNullStream() {
        assertThrows(IllegalArgumentException.class, () -> StreamUtils.getStreamContents(null));
        assertThrows(IllegalArgumentException.class, () -> StreamUtils.getGzipStreamContents(null));
    }
}
