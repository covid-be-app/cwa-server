/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * All modifications are copyright (c) 2020 Devside SRL.
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

import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


public class HttpHeaderBuilder {

  public static final String SECRET_KEY = "+VhBgVyOB96AX1NHqEyibA==";
  public static final String RANDOM_STRING = "uyVJlD1sfiSZkHDR";
  public static final LocalDate DATE_PATIENT_INFECTUOUS = LocalDate.parse("2020-08-04");
  public static final LocalDate DATE_TEST_COMMUNICATED = LocalDate.parse("2020-08-04");
  public static final LocalDate DATE_ONSET_OF_SYMPTOMS = LocalDate.parse("2020-08-04");
  public static final String RESULT_CHANNEL = "1";
  public static final String COVICODE = "123456789012";
  private final HttpHeaders headers = new HttpHeaders();

  public static HttpHeaderBuilder builder() {
    return new HttpHeaderBuilder();
  }

  public HttpHeaderBuilder contentTypeProtoBuf() {
    headers.setContentType(MediaType.valueOf("application/x-protobuf"));
    return this;
  }

  public HttpHeaderBuilder secretKey() {
    return secretKey(SECRET_KEY);
  }

  public HttpHeaderBuilder randomString() {
    return randomString(RANDOM_STRING);
  }

  public HttpHeaderBuilder datePatientInfectious() {
    return datePatientInfectious(DATE_PATIENT_INFECTUOUS);
  }

  public HttpHeaderBuilder dateOnsetOfSymptoms() {
    return dateOnsetOfSymptoms(LocalDate.now().minusDays(5));
  }

  public HttpHeaderBuilder dateTestCommunicated() {
    return dateTestCommunicated(DATE_TEST_COMMUNICATED);
  }

  public HttpHeaderBuilder resultChannel() {
    return resultChannel(RESULT_CHANNEL);
  }

  public HttpHeaderBuilder coviCode() {
    return coviCode(COVICODE);
  }



  public HttpHeaderBuilder secretKey(String secretKey) {
    headers.set("Secret-Key", secretKey);
    return this;
  }

  public HttpHeaderBuilder randomString(String randomString) {
    headers.set("Random-String", randomString);
    return this;
  }

  public HttpHeaderBuilder datePatientInfectious(LocalDate datePatientInfectious) {
    headers.set("Date-Patient-Infectious", datePatientInfectious.toString());
    return this;
  }

  public HttpHeaderBuilder dateOnsetOfSymptoms(LocalDate dateOnsetOfSymptoms) {
    headers.set("Date-Onset-Of-Symptoms", dateOnsetOfSymptoms.toString());
    return this;
  }

  public HttpHeaderBuilder dateTestCommunicated(LocalDate datePatientCommunicated) {
    headers.set("Date-Test-Communicated", datePatientCommunicated.toString());
    return this;
  }

  public HttpHeaderBuilder resultChannel(String resultChannel) {
    headers.set("Result-Channel", resultChannel);
    return this;
  }

  private HttpHeaderBuilder coviCode(String covicode) {
    headers.set("Covi-Code", covicode);
    return this;
  }


  public HttpHeaders build() {
    return headers;
  }
}
