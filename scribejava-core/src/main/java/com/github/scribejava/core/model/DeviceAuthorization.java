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

/**
 * Device Authorization Response
 *
 * @see <a href="https://tools.ietf.org/html/rfc8628#section-3.2">rfc8628</a>
 */
public class DeviceAuthorization {

  /**
   * device_code
   *
   * <p>REQUIRED. The device verification code.
   */
  private final String deviceCode;

  /**
   * user_code
   *
   * <p>REQUIRED. The end-user verification code.
   */
  private final String userCode;

  /**
   * verification_uri
   *
   * <p>REQUIRED. The end-user verification URI on the authorization server. The URI should be short
   * and easy to remember as end users will be asked to manually type it into their user agent.
   */
  private final String verificationUri;
  /**
   * expires_in
   *
   * <p>REQUIRED. The lifetime in seconds of the "device_code" and "user_code".
   */
  private final int expiresInSeconds;
  /**
   * verification_uri_complete
   *
   * <p>OPTIONAL. A verification URI that includes the "user_code" (or other information with the
   * same function as the "user_code"), which is designed for non-textual transmission.
   */
  private String verificationUriComplete;
  /**
   * interval
   *
   * <p>OPTIONAL. The minimum amount of time in seconds that the client SHOULD wait between polling
   * requests to the token endpoint. If no value is provided, clients MUST use 5 as the default.
   */
  private int intervalSeconds = 5;

  public DeviceAuthorization(
      String deviceCode, String userCode, String verificationUri, int expiresInSeconds) {
    this.deviceCode = deviceCode;
    this.userCode = userCode;
    this.verificationUri = verificationUri;
    this.expiresInSeconds = expiresInSeconds;
  }

  public String getDeviceCode() {
    return deviceCode;
  }

  public String getUserCode() {
    return userCode;
  }

  public String getVerificationUri() {
    return verificationUri;
  }

  public String getVerificationUriComplete() {
    return verificationUriComplete;
  }

  public void setVerificationUriComplete(String verificationUriComplete) {
    this.verificationUriComplete = verificationUriComplete;
  }

  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  public int getIntervalSeconds() {
    return intervalSeconds;
  }

  public void setIntervalSeconds(int intervalSeconds) {
    this.intervalSeconds = intervalSeconds;
  }

  @Override
  public String toString() {
    return "DeviceAuthorization{"
        + "'deviceCode'='"
        + deviceCode
        + "', 'userCode'='"
        + userCode
        + "', 'verificationUri'='"
        + verificationUri
        + "', 'verificationUriComplete'='"
        + verificationUriComplete
        + "', 'expiresInSeconds'='"
        + expiresInSeconds
        + "', 'intervalSeconds'='"
        + intervalSeconds
        + "'}";
  }
}
