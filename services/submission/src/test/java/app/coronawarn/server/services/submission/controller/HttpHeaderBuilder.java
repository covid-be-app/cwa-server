/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.submission.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


public class HttpHeaderBuilder {

  private final HttpHeaders headers = new HttpHeaders();

  public static HttpHeaderBuilder builder() {
    return new HttpHeaderBuilder();
  }

  public HttpHeaderBuilder contentTypeProtoBuf() {
    headers.setContentType(MediaType.valueOf("application/x-protobuf"));
    return this;
  }

  public HttpHeaderBuilder secretKey() {
    headers.set("Secret-Key", "secretKey");
    return this;
  }

  public HttpHeaderBuilder randomString() {
    headers.set("Random-String", "randomString");
    return this;
  }

  public HttpHeaderBuilder datePatientInfectious() {
    headers.set("Date-Patient-Infectious", "2020-08-04");
    return this;
  }

  public HttpHeaderBuilder dateTestCommunicated() {
    headers.set("Date-Test-Communicated", "2020-08-04");
    return this;
  }


  public HttpHeaders build() {
    return headers;
  }
}
