package com.github.scribejava.core.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OAuthEncoderTest {

    @Test
    public void shouldPercentEncodeString() {
        final String plain = "this is a test &^";
        final String encoded = "this%20is%20a%20test%20%26%5E";
        assertThat(OAuthEncoder.encode(plain)).isEqualTo(encoded);
    }

    @Test
    public void shouldFormURLDecodeString() {
        final String encoded = "this+is+a+test+%26%5E";
        final String plain = "this is a test &^";
        assertThat(OAuthEncoder.decode(encoded)).isEqualTo(plain);
    }

    @Test
    public void shouldPercentEncodeAllSpecialCharacters() {
        final String plain = "!*'();:@&=+$,/?#[]";
        final String encoded = "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%2F%3F%23%5B%5D";
        assertThat(OAuthEncoder.encode(plain)).isEqualTo(encoded);
        assertThat(OAuthEncoder.decode(encoded)).isEqualTo(plain);
    }

    @Test
    public void shouldNotPercentEncodeReservedCharacters() {
        final String plain = "abcde123456-._~";
        final String encoded = plain;
        assertThat(OAuthEncoder.encode(plain)).isEqualTo(encoded);
    }

    @Test
    public void shouldThrowExceptionIfStringToEncodeIsNull() {
        assertThatThrownBy(() -> OAuthEncoder.encode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionIfStringToDecodeIsNull() {
        assertThatThrownBy(() -> OAuthEncoder.decode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldPercentEncodeCorrectlyTwitterCodingExamples() {
        // These tests are part of the Twitter dev examples here
        // -> https://dev.twitter.com/docs/auth/percent-encoding-parameters
        final String[] sources = {"Ladies + Gentlemen", "An encoded string!", "Dogs, Cats & Mice"};
        final String[] encoded = {"Ladies%20%2B%20Gentlemen", "An%20encoded%20string%21",
                "Dogs%2C%20Cats%20%26%20Mice"};

        for (int i = 0; i < sources.length; i++) {
            assertThat(OAuthEncoder.encode(sources[i])).isEqualTo(encoded[i]);
        }
    }
}
