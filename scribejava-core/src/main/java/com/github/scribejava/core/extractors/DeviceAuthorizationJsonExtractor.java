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
package com.github.scribejava.core.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.model.Response;
import java.io.IOException;

public class DeviceAuthorizationJsonExtractor extends AbstractJsonExtractor {

  protected DeviceAuthorizationJsonExtractor() {}

  public static DeviceAuthorizationJsonExtractor instance() {
    return InstanceHolder.INSTANCE;
  }

  public DeviceAuthorization extract(Response response) throws IOException {
    if (response.getCode() != 200) {
      generateError(response);
    }
    return createDeviceAuthorization(response.getBody());
  }

  public void generateError(Response response) throws IOException {
    OAuth2AccessTokenJsonExtractor.instance().generateError(response);
  }

  private DeviceAuthorization createDeviceAuthorization(String rawResponse) throws IOException {

    final JsonNode response = OBJECT_MAPPER.readTree(rawResponse);

    final DeviceAuthorization deviceAuthorization =
        new DeviceAuthorization(
            extractRequiredParameter(response, "device_code", rawResponse).asText(),
            extractRequiredParameter(response, "user_code", rawResponse).asText(),
            extractRequiredParameter(response, getVerificationUriParamName(), rawResponse).asText(),
            extractRequiredParameter(response, "expires_in", rawResponse).asInt());

    final JsonNode intervalSeconds = response.get("interval");
    if (intervalSeconds != null && !intervalSeconds.isNull()) {
      deviceAuthorization.setIntervalSeconds(intervalSeconds.asInt(5));
    }

    final JsonNode verificationUriComplete = response.get("verification_uri_complete");
    if (verificationUriComplete != null && !verificationUriComplete.isNull()) {
      deviceAuthorization.setVerificationUriComplete(verificationUriComplete.asText());
    }

    return deviceAuthorization;
  }

  protected String getVerificationUriParamName() {
    return "verification_uri";
  }

  private static class InstanceHolder {

    private static final DeviceAuthorizationJsonExtractor INSTANCE =
        new DeviceAuthorizationJsonExtractor();
  }
}
