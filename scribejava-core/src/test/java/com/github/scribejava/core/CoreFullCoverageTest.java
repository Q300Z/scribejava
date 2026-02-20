package com.github.scribejava.core;

import com.github.scribejava.core.utils.Preconditions;
import com.github.scribejava.core.utils.StreamUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CoreFullCoverageTest {

    @Test
    public void testPreconditions() {
        Preconditions.checkNotNull("not null", "error");
        Preconditions.checkEmptyString("not empty", "error");

        final IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkNotNull(null, "is null"));
        assertThat(ex1.getMessage()).isEqualTo("is null");

        final IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkEmptyString("", "is empty"));
        assertThat(ex2.getMessage()).isEqualTo("is empty");

        final IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkEmptyString(null, "is null string"));
        assertThat(ex3.getMessage()).isEqualTo("is null string");
    }

    @Test
    public void testStreamUtils() throws Exception {
        final String data = "hello world";
        try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
            assertThat(StreamUtils.getStreamContents(is)).isEqualTo(data);
        }
    }
}
