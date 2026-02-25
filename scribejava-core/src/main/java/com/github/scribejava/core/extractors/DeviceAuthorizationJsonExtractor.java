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

import com.github.scribejava.core.model.DeviceAuthorization;
import com.github.scribejava.core.utils.JsonUtils;
import java.io.IOException;
import java.util.Map;

/** Extracteur JSON natif pour le Device Flow (RFC 8628). */
public class DeviceAuthorizationJsonExtractor extends AbstractJsonExtractor<DeviceAuthorization> {

  protected DeviceAuthorizationJsonExtractor() {}

  public static DeviceAuthorizationJsonExtractor instance() {
    return InstanceHolder.INSTANCE;
  }

  @Override
  protected DeviceAuthorization createToken(String body) throws IOException {
    final Map<String, Object> response = JsonUtils.parse(body);

    final String deviceCode = (String) extractRequiredParameter(response, "device_code", body);
    final String userCode = (String) extractRequiredParameter(response, "user_code", body);
    final String verificationUri =
        (String) extractRequiredParameter(response, "verification_uri", body);
    final int expiresIn = parseAsInt(response.get("expires_in"));

    final DeviceAuthorization deviceAuthorization =
        new DeviceAuthorization(deviceCode, userCode, verificationUri, expiresIn);

    deviceAuthorization.setVerificationUriComplete(
        (String) response.get("verification_uri_complete"));

    final Object intervalObj = response.get("interval");
    if (intervalObj != null) {
      deviceAuthorization.setIntervalSeconds(parseAsInt(intervalObj));
    }

    return deviceAuthorization;
  }

  private int parseAsInt(Object obj) {
    if (obj instanceof Number) {
      return ((Number) obj).intValue();
    } else if (obj instanceof String) {
      return Integer.parseInt((String) obj);
    }
    throw new IllegalArgumentException("Invalid type for integer parameter: " + obj.getClass());
  }

  private static class InstanceHolder {
    private static final DeviceAuthorizationJsonExtractor INSTANCE =
        new DeviceAuthorizationJsonExtractor();
  }
}
