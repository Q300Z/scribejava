/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.scribejava.oauth1.services;

import com.github.scribejava.core.exceptions.OAuthSignatureException;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class RSASha1SignatureAdvancedTest {

    @Test
    public void shouldGenerateValidSignatureWithRealKey() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        final KeyPair pair = keyGen.generateKeyPair();
        final PrivateKey privateKey = pair.getPrivate();

        final RSASha1SignatureService service = new RSASha1SignatureService(privateKey);
        final String baseString = "GET&http%3A%2F%2Fexample.com&oauth_consumer_key%3Dkey";

        final String signature = service.getSignature(baseString, "api-secret", "token-secret");

        assertThat(signature).isNotNull();
        assertThat(signature).isNotEmpty();
        assertThat(service.getSignatureMethod()).isEqualTo("RSA-SHA1");
    }

    @Test
    public void shouldThrowExceptionOnSignatureError() {
        // We use a mock PrivateKey that might cause an error during initSign or sign
        final PrivateKey invalidKey = mock(PrivateKey.class);
        final RSASha1SignatureService service = new RSASha1SignatureService(invalidKey);

        assertThatThrownBy(() -> service.getSignature("any", "secret", "secret"))
                .isInstanceOf(OAuthSignatureException.class);
    }
}
