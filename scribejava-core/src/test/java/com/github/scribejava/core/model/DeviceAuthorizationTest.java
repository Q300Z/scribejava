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
package com.github.scribejava.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Tests pour la classe DeviceAuthorization. */
public class DeviceAuthorizationTest {

  /**
   * Vérifie le fonctionnement des constructeurs, accesseurs, mutateurs, valeurs par défaut et
   * toString de DeviceAuthorization.
   */
  @Test
  public void testDeviceAuthorization() {
    final DeviceAuthorization deviceAuth =
        new DeviceAuthorization(
            "device_code_123", "user_code_456", "https://verify.example.com", 3600);

    assertThat(deviceAuth.getDeviceCode()).isEqualTo("device_code_123");
    assertThat(deviceAuth.getUserCode()).isEqualTo("user_code_456");
    assertThat(deviceAuth.getVerificationUri()).isEqualTo("https://verify.example.com");
    assertThat(deviceAuth.getExpiresInSeconds()).isEqualTo(3600);

    // Default interval seconds should be 5
    assertThat(deviceAuth.getIntervalSeconds()).isEqualTo(5);
    assertThat(deviceAuth.getVerificationUriComplete()).isNull();

    // Setters
    deviceAuth.setIntervalSeconds(10);
    deviceAuth.setVerificationUriComplete("https://verify.example.com?code=user_code_456");

    assertThat(deviceAuth.getIntervalSeconds()).isEqualTo(10);
    assertThat(deviceAuth.getVerificationUriComplete())
        .isEqualTo("https://verify.example.com?code=user_code_456");

    // toString check
    final String str = deviceAuth.toString();
    assertThat(str).contains("device_code_123");
    assertThat(str).contains("user_code_456");
    assertThat(str).contains("https://verify.example.com");
    assertThat(str).contains("https://verify.example.com?code=user_code_456");
    assertThat(str).contains("3600");
    assertThat(str).contains("10");
  }
}
