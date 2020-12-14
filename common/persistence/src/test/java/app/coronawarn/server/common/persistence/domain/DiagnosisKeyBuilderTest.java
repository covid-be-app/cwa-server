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

package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilder.INVALID_MAX_TRANSMISSION_RISK_LEVEL;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilder.MAX_TRANSMISSION_RISK_LEVEL;
import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static app.coronawarn.server.common.persistence.service.DiagnosisKeyServiceTestHelper.buildDiagnosisKeyForSubmissionTimestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DiagnosisKeyBuilderTest {

  private static final String COUNTRY = "BEL";
  private static final String MOBILE_TEST_ID = "123456789012345";
  private static final LocalDate DATE_PATIENT_INFECTIOUS = LocalDate.parse("2020-08-15");
  private static final LocalDate DATE_TEST_COMMUNICATED = LocalDate.parse("2020-08-15");
  private static final int RESULT_CHANNEL = 1;
  private final byte[] expKeyData = "16-bytelongarray".getBytes(StandardCharsets.US_ASCII);
  private final int expRollingStartIntervalNumber = 73800;
  private final int expTransmissionRiskLevel = 1;
  private final long expSubmissionTimestamp = 2L;

  private final boolean expConsentToFederation = false;
  private final String originCountry = "BE";
  private final Set<String> visitedCountries = new HashSet<>(Set.of("BE"));
  private final ReportType reportType = ReportType.CONFIRMED_TEST;
  private final int daysSinceOnsetOfSymptoms = 2;

  @Test
  void buildFromProtoBufObjWithSubmissionTimestamp() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartIntervalNumber(this.expRollingStartIntervalNumber)
        .setRollingPeriod(DiagnosisKey.EXPECTED_ROLLING_PERIOD)
        .setTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .setReportType(reportType)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromTemporaryExposureKeyAndMetadata(protoBufObj, List.of("BE"), "BE", true)
        .withSubmissionTimestamp(this.expSubmissionTimestamp)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp,this.expTransmissionRiskLevel);
  }

  @Test
  void buildFromProtoBufObjWithoutSubmissionTimestamp() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(expKeyData))
        .setRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .setTransmissionRiskLevel(expTransmissionRiskLevel)
        .setReportType(reportType)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromTemporaryExposureKeyAndMetadata(protoBufObj, List.of("BE"), "BE", true)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }


  @Test
  void buildFromProtoBufObjWithInvalidMaxTransmissionRiskLevel() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(this.expKeyData))
        .setRollingStartIntervalNumber(this.expRollingStartIntervalNumber)
        .setRollingPeriod(DiagnosisKey.EXPECTED_ROLLING_PERIOD)
        .setTransmissionRiskLevel(INVALID_MAX_TRANSMISSION_RISK_LEVEL)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromTemporaryExposureKeyAndMetadata(protoBufObj, List.of("BE"), "BE", true)
        .withSubmissionTimestamp(this.expSubmissionTimestamp)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp,MAX_TRANSMISSION_RISK_LEVEL);
  }

  @Test
  void buildSuccessivelyWithSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(this.expKeyData)
        .withRollingStartIntervalNumber(this.expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .withSubmissionTimestamp(this.expSubmissionTimestamp)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, this.expSubmissionTimestamp,this.expTransmissionRiskLevel);
  }

  @Test
  void buildSuccessivelyWithoutSubmissionTimestamp() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(this.expKeyData)
        .withRollingStartIntervalNumber(this.expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(this.expTransmissionRiskLevel)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  void buildSuccessivelyWithoutVisitedCountries() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  @Test
  void buildSuccessivelyWithOriginMissingFromVisitedCountries() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(Set.of("IT"))
        .build();

    visitedCountries.add("IT");
    assertDiagnosisKeyEquals(actDiagnosisKey);
  }


  @Test
  void buildSuccessivelyWithRollingPeriod() {
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withSubmissionTimestamp(expSubmissionTimestamp)
        .withRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withReportType(reportType)
        .withDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .withConsentToFederation(expConsentToFederation)
        .withCountryCode(originCountry)
        .withVisitedCountries(visitedCountries)
        .build();

    assertDiagnosisKeyEquals(actDiagnosisKey, expSubmissionTimestamp,this.expTransmissionRiskLevel);
  }

  @ParameterizedTest
  @ValueSource(ints = {4200, 441552})
  void rollingStartIntervalNumberDoesNotThrowForValid(int validRollingStartIntervalNumber) {
    assertThatCode(() -> keyWithRollingStartIntervalNumber(validRollingStartIntervalNumber)).doesNotThrowAnyException();
  }

  @Test
  void rollingStartIntervalNumberCannotBeInFuture() {
    assertThat(catchThrowable(() -> keyWithRollingStartIntervalNumber(Integer.MAX_VALUE)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            "[Rolling start interval number must be greater 0 and cannot be in the future. Invalid Value: "
                + Integer.MAX_VALUE + "]");

    long tomorrow = LocalDate
        .ofInstant(Instant.now(), ZoneOffset.UTC)
        .plusDays(1).atStartOfDay()
        .toEpochSecond(ZoneOffset.UTC);

    assertThat(catchThrowable(() -> keyWithRollingStartIntervalNumber((int) tomorrow)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            String.format(
                "[Rolling start interval number must be greater 0 and cannot be in the future. Invalid Value: %s]",
                tomorrow));
  }

  @Test
  void failsForInvalidRollingStartIntervalNumber() {
    assertThat(
        catchThrowable(() -> DiagnosisKey.builder()
            .withKeyData(this.expKeyData)
            .withRollingStartIntervalNumber(0)
            .withTransmissionRiskLevel(this.expTransmissionRiskLevel)
            .withCountryCode(originCountry)
            .build()
        )
    ).isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"DER", "xx", "Be", "bE", "BE,FRE",""})
  void failsForInvalidOriginCountry(String countryCode) {
    assertThat(
        catchThrowable(() -> DiagnosisKey.builder()
            .withKeyData(expKeyData)
            .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
            .withTransmissionRiskLevel(expTransmissionRiskLevel)
            .withCountryCode(countryCode)
            .build()
        )
    ).isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"DER", "xx", "Be", "bE", "BE,FRE",""})
  void failsForInvalidVisitedCountries(String visitedCountries) {
    assertThat(
        catchThrowable(() -> DiagnosisKey.builder()
            .withKeyData(expKeyData)
            .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
            .withTransmissionRiskLevel(expTransmissionRiskLevel)
            .withCountryCode("BE")
            .withVisitedCountries(Set.of(visitedCountries))
            .withCountryCode("BE")
            .build()
        )
    ).isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = {DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL - 1, DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL + 1})
  void transmissionRiskLevelMustBeInRange(int invalidRiskLevel) {
    assertThat(catchThrowable(() -> keyWithRiskLevel(invalidRiskLevel)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            "[Risk level must be between " + DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL + " and "
                + DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL + ". Invalid Value: " + invalidRiskLevel + "]");
  }

  @ParameterizedTest
  @ValueSource(ints = {DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL, DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL})
  void transmissionRiskLevelDoesNotThrowForValid(int validRiskLevel) {
    assertThatCode(() -> keyWithRiskLevel(validRiskLevel)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, -17, 4001})
  void daysSinceOnsetSyptomsMustBeInRange(int invalidDsos) {
    assertThat(catchThrowable(() -> keyWithDsos(invalidDsos)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage(
            "[Days since onset of symptoms value must be between -14 and 4000. Invalid Value: " + invalidDsos + "]");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, -14, 3986})
  void daysSinceOnsetSyptomsValidationDoesNotThrowForValid(int validDsos) {
    assertThatCode(() -> keyWithDsos(validDsos)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(ints = {-3, 145})
  void rollingPeriodMustBeExpectedValue(int invalidRollingPeriod) {
    assertThat(catchThrowable(() -> keyWithRollingPeriod(invalidRollingPeriod)))
        .isInstanceOf(InvalidDiagnosisKeyException.class)
        .hasMessage("[Rolling period must be between " + DiagnosisKey.MIN_ROLLING_PERIOD + " and "
            + DiagnosisKey.MAX_ROLLING_PERIOD
            + ". Invalid Value: " + invalidRollingPeriod + "]");
  }

  @ParameterizedTest
  @ValueSource(ints = {DiagnosisKey.MIN_ROLLING_PERIOD, 100, DiagnosisKey.MAX_ROLLING_PERIOD})
  void rollingPeriodDoesNotThrowForValid(int validRollingPeriod) {
    assertThatCode(() -> keyWithRollingPeriod(validRollingPeriod)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"17--bytelongarray", "", "1"})
  void keyDataMustHaveValidLength(String invalidKeyString) {
    assertThat(
        catchThrowable(() -> keyWithKeyData(invalidKeyString.getBytes(StandardCharsets.US_ASCII))))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @Test
  void keyDataDoesNotThrowOnValid() {
    assertThatCode(() -> keyWithKeyData("16-bytelongarray".getBytes(StandardCharsets.US_ASCII)))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(longs = {-1L, Long.MAX_VALUE})
  void submissionTimestampMustBeValid(long submissionTimestamp) {
    assertThat(
        catchThrowable(() -> buildDiagnosisKeyForSubmissionTimestamp(submissionTimestamp)))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @Test
  void submissionTimestampMustNotBeInTheFuture() {
    assertThat(catchThrowable(
        () -> buildDiagnosisKeyForSubmissionTimestamp(getCurrentHoursSinceEpoch() + 1)))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
    assertThat(catchThrowable(() -> buildDiagnosisKeyForSubmissionTimestamp(
        Instant.now().getEpochSecond() /* accidentally forgot to divide by SECONDS_PER_HOUR */)))
        .isInstanceOf(InvalidDiagnosisKeyException.class);
  }

  @Test
  void submissionTimestampDoesNotThrowOnValid() {
    assertThatCode(() -> buildDiagnosisKeyForSubmissionTimestamp(1L, 144, false)).doesNotThrowAnyException();
    assertThatCode(() -> buildDiagnosisKeyForSubmissionTimestamp(getCurrentHoursSinceEpoch(), 144, false))
        .doesNotThrowAnyException();
    assertThatCode(
        () -> buildDiagnosisKeyForSubmissionTimestamp(
            Instant.now().minus(Duration.ofHours(2)).getEpochSecond() / SECONDS_PER_HOUR, 144, false))
        .doesNotThrowAnyException();
  }

  @Test
  void testKeyHasDefaultForReportType() {
    TemporaryExposureKey protoBufObj = TemporaryExposureKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(expKeyData))
        .setRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .setTransmissionRiskLevel(expTransmissionRiskLevel)
        .build();

    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromTemporaryExposureKeyAndMetadata(protoBufObj, List.of("BE"), "BE", true)
        .build();

    assertThat(actDiagnosisKey.getReportType()).isEqualTo(reportType);
  }

  @Test
  void testKeyBuildsSuccessfullyFromFederationDiagnosisKey() {
    app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey federationKey = app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey
        .newBuilder()
        .setKeyData(ByteString.copyFrom(expKeyData))
        .addAllVisitedCountries(visitedCountries)
        .setRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .setTransmissionRiskLevel(expTransmissionRiskLevel)
        .setRollingPeriod(DiagnosisKey.MAX_ROLLING_PERIOD)
        .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
        .setReportType(reportType)
        .setOrigin(originCountry)
        .build();
    DiagnosisKey actDiagnosisKey = DiagnosisKey.builder()
        .fromFederationDiagnosisKey(federationKey).build();
    assertDiagnosisKeyEquals(actDiagnosisKey);
  }

  private DiagnosisKey keyWithKeyData(byte[] expKeyData) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withCountryCode(originCountry)
        .build();
  }

  private DiagnosisKey keyWithRollingStartIntervalNumber(int expRollingStartIntervalNumber) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withCountryCode(originCountry)
        .build();
  }

  private DiagnosisKey keyWithRollingPeriod(int expRollingPeriod) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withCountryCode(originCountry)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withRollingPeriod(expRollingPeriod).build();
  }

  private DiagnosisKey keyWithRiskLevel(int expTransmissionRiskLevel) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withCountryCode(originCountry)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .build();
  }

  private DiagnosisKey keyWithDsos(int dsos) {
    return DiagnosisKey.builder()
        .withKeyData(expKeyData)
        .withRollingStartIntervalNumber(expRollingStartIntervalNumber)
        .withTransmissionRiskLevel(expTransmissionRiskLevel)
        .withCountryCode(originCountry)
        .withMobileTestId(MOBILE_TEST_ID)
        .withDatePatientInfectious(DATE_PATIENT_INFECTIOUS)
        .withDateTestCommunicated(DATE_TEST_COMMUNICATED)
        .withResultChannel(RESULT_CHANNEL)
        .withDaysSinceOnsetOfSymptoms(dsos)
        .build();
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey) {
    assertDiagnosisKeyEquals(actDiagnosisKey, getCurrentHoursSinceEpoch(),this.expTransmissionRiskLevel);
  }

  private long getCurrentHoursSinceEpoch() {
    return Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
  }

  private void assertDiagnosisKeyEquals(DiagnosisKey actDiagnosisKey,
      long expSubmissionTimestamp, int expTransmissionRiskLevel) {
    assertThat(actDiagnosisKey.getKeyData()).isEqualTo(expKeyData);
    assertThat(actDiagnosisKey.getRollingStartIntervalNumber()).isEqualTo(expRollingStartIntervalNumber);
    assertThat(actDiagnosisKey.getRollingPeriod()).isEqualTo(DiagnosisKey.MAX_ROLLING_PERIOD);
    assertThat(actDiagnosisKey.getTransmissionRiskLevel()).isEqualTo(expTransmissionRiskLevel);
    assertThat(actDiagnosisKey.getSubmissionTimestamp()).isEqualTo(expSubmissionTimestamp);
    assertThat(actDiagnosisKey.getReportType()).isEqualTo(reportType);
    assertThat(actDiagnosisKey.getDaysSinceOnsetOfSymptoms()).isEqualTo(daysSinceOnsetOfSymptoms);
    assertThat(actDiagnosisKey.isConsentToFederation()).isEqualTo(expConsentToFederation);
    assertThat(actDiagnosisKey.getOriginCountry()).isEqualTo(originCountry);
    assertThat(actDiagnosisKey.getVisitedCountries()).isEqualTo(visitedCountries);

  }
}
