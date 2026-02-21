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
package com.github.scribejava.core.httpclient.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartUtils {

  private static final String B_CHARS_NO_SPACE_PATTERN = "0-9a-zA-Z'()+_,-./:=?";
  private static final String B_CHARS_PATTERN = B_CHARS_NO_SPACE_PATTERN + " ";
  private static final String BOUNDARY_PATTERN =
      '[' + B_CHARS_PATTERN + "]{0,69}[" + B_CHARS_NO_SPACE_PATTERN + ']';
  private static final Pattern BOUNDARY_REGEXP = Pattern.compile(BOUNDARY_PATTERN);
  private static final Pattern BOUNDARY_FROM_HEADER_REGEXP =
      Pattern.compile("; boundary=\"?(" + BOUNDARY_PATTERN + ")\"?");

  private MultipartUtils() {}

  public static void checkBoundarySyntax(String boundary) {
    if (boundary == null || !BOUNDARY_REGEXP.matcher(boundary).matches()) {
      throw new IllegalArgumentException(
          "{'boundary'='"
              + boundary
              + "'} has invalid syntax. Should be '"
              + BOUNDARY_PATTERN
              + "'.");
    }
  }

  public static String parseBoundaryFromHeader(String contentTypeHeader) {
    if (contentTypeHeader == null) {
      return null;
    }
    final Matcher matcher = BOUNDARY_FROM_HEADER_REGEXP.matcher(contentTypeHeader);
    return matcher.find() ? matcher.group(1) : null;
  }

  public static String generateDefaultBoundary() {
    return "----ScribeJava----" + Instant.now().toEpochMilli();
  }

  public static ByteArrayOutputStream getPayload(MultipartPayload multipartPayload)
      throws IOException {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();

    final String preamble = multipartPayload.getPreamble();
    if (preamble != null) {
      os.write((preamble + "\r\n").getBytes());
    }
    final List<BodyPartPayload> bodyParts = multipartPayload.getBodyParts();
    if (!bodyParts.isEmpty()) {
      final String boundary = multipartPayload.getBoundary();
      final byte[] startBoundary = ("--" + boundary + "\r\n").getBytes();

      for (BodyPartPayload bodyPart : bodyParts) {
        os.write(startBoundary);

        final Map<String, String> bodyPartHeaders = bodyPart.getHeaders();
        if (bodyPartHeaders != null) {
          bodyPartHeaders.forEach(
              (key, value) -> {
                try {
                  os.write((key + ": " + value + "\r\n").getBytes());
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
        }

        os.write("\r\n".getBytes());
        if (bodyPart instanceof MultipartPayload) {
          getPayload((MultipartPayload) bodyPart).writeTo(os);
        } else if (bodyPart instanceof ByteArrayBodyPartPayload) {
          final ByteArrayBodyPartPayload byteArrayBodyPart = (ByteArrayBodyPartPayload) bodyPart;
          os.write(
              byteArrayBodyPart.getPayload(),
              byteArrayBodyPart.getOff(),
              byteArrayBodyPart.getLen());
        } else {
          throw new AssertionError(bodyPart.getClass());
        }
        os.write("\r\n".getBytes()); // CRLF for the next (starting or closing) boundary
      }

      os.write(("--" + boundary + "--").getBytes());
      final String epilogue = multipartPayload.getEpilogue();
      if (epilogue != null) {
        os.write(("\r\n" + epilogue).getBytes());
      }
    }
    return os;
  }
}
