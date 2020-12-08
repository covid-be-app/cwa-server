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

import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.FinalBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.RollingStartIntervalNumberBuilder;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.TransmissionRiskLevelBuilder;
import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;

import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this builder can be retrieved by calling {@link DiagnosisKey#builder()}. A {@link DiagnosisKey} can
 * then be build by either providing the required member values or by passing the respective protocol buffer object.
 */
public class DiagnosisKeyBuilder implements
    Builder, RollingStartIntervalNumberBuilder, TransmissionRiskLevelBuilder, FinalBuilder {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBuilder.class);
  public static final int INVALID_MAX_TRANSMISSION_RISK_LEVEL = 8;
  public static final int MAX_TRANSMISSION_RISK_LEVEL = 7;

  private byte[] keyData;
  private int rollingStartIntervalNumber;
  private int rollingPeriod = DiagnosisKey.EXPECTED_ROLLING_PERIOD;
  private int transmissionRiskLevel;
  private Long submissionTimestamp = null;
  private String mobileTestId;
  private String mobileTestId2;
  private LocalDate datePatientInfectious;
  private LocalDate dateTestCommunicated;
  private int resultChannel;
  private String countryCode;
  private Set<String> visitedCountries;
  private ReportType reportType;
  private boolean consentToFederation;
  private Integer daysSinceOnsetOfSymptoms;
  private DiagnosisKeyNormalizer fieldNormalizer;
  private boolean verified;

  DiagnosisKeyBuilder() {
  }

  @Override
  public RollingStartIntervalNumberBuilder withKeyData(byte[] keyData) {
    this.keyData = keyData;
    return this;
  }

  @Override
  public TransmissionRiskLevelBuilder withRollingStartIntervalNumber(int rollingStartIntervalNumber) {
    this.rollingStartIntervalNumber = rollingStartIntervalNumber;
    return this;
  }

  @Override
  public FinalBuilder withTransmissionRiskLevel(Integer transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  //  @Override
  //  public FinalBuilder fromProtoBuf(TemporaryExposureKey protoBufObject) {
  //    return this
  //        .withKeyData(protoBufObject.getKeyData().toByteArray())
  //        .withRollingStartIntervalNumber(protoBufObject.getRollingStartIntervalNumber())
  //        .withTransmissionRiskLevel(performCorrectionOnTransmissionRiskLevel(protoBufObject))
  //        .withRollingPeriod(protoBufObject.getRollingPeriod());
  //  }

  @Override
  public FinalBuilder fromTemporaryExposureKeyAndMetadata(TemporaryExposureKey protoBufObject,
      List<String> visitedCountries, String originCountry, boolean consentToFederation) {
    return this
        .withKeyData(protoBufObject.getKeyData().toByteArray())
        .withRollingStartIntervalNumber(protoBufObject.getRollingStartIntervalNumber())
        //.withTransmissionRiskLevel(
        //    protoBufObject.hasTransmissionRiskLevel() ? protoBufObject.getTransmissionRiskLevel() : null)
        .withTransmissionRiskLevel(performCorrectionOnTransmissionRiskLevel(protoBufObject))

        // TODO: check this : performCorrectionOnTransmissionRiskLevel(protoBufObject))


        .withRollingPeriod(protoBufObject.getRollingPeriod())
        .withReportType(protoBufObject.getReportType()).withDaysSinceOnsetOfSymptoms(
            protoBufObject.hasDaysSinceOnsetOfSymptoms() ? protoBufObject.getDaysSinceOnsetOfSymptoms() : null)
        .withVisitedCountries(new HashSet<>(visitedCountries))
        .withCountryCode(originCountry)
        .withConsentToFederation(consentToFederation);
  }

  @Override
  public FinalBuilder fromFederationDiagnosisKey(
      app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey federationDiagnosisKey) {
    return this
        .withKeyData(federationDiagnosisKey.getKeyData().toByteArray())
        .withRollingStartIntervalNumber(federationDiagnosisKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(federationDiagnosisKey.getTransmissionRiskLevel())
        .withRollingPeriod(federationDiagnosisKey.getRollingPeriod())
        .withCountryCode(federationDiagnosisKey.getOrigin())
        .withReportType(federationDiagnosisKey.getReportType())
        .withVisitedCountries(new HashSet<>(federationDiagnosisKey.getVisitedCountriesList()))
        .withDaysSinceOnsetOfSymptoms(federationDiagnosisKey.getDaysSinceOnsetOfSymptoms());
  }

  private Integer performCorrectionOnTransmissionRiskLevel(TemporaryExposureKey protoBufObject) {
    // TODO: clean this up....
    if (protoBufObject.getTransmissionRiskLevel() == INVALID_MAX_TRANSMISSION_RISK_LEVEL) {
      return MAX_TRANSMISSION_RISK_LEVEL;
    } else if (protoBufObject.getTransmissionRiskLevel() == 0) {
      return 1;
    } else {
      return protoBufObject.getTransmissionRiskLevel();
    }
  }

  @Override
  public FinalBuilder withSubmissionTimestamp(long submissionTimestamp) {
    this.submissionTimestamp = submissionTimestamp;
    return this;
  }

  @Override
  public FinalBuilder withMobileTestId(String mobileTestId) {
    this.mobileTestId = mobileTestId;
    return this;
  }

  @Override
  public FinalBuilder withMobileTestId2(String mobileTestId2) {
    this.mobileTestId2 = mobileTestId2;
    return this;
  }

  @Override
  public FinalBuilder withDatePatientInfectious(LocalDate datePatientInfectious) {
    this.datePatientInfectious = datePatientInfectious;
    return this;
  }

  @Override
  public FinalBuilder withDateTestCommunicated(LocalDate dateTestCommunicated) {
    this.dateTestCommunicated = dateTestCommunicated;
    return this;
  }

  @Override
  public FinalBuilder withResultChannel(int resultChannel) {
    this.resultChannel = resultChannel;
    return this;
  }

  @Override
  public FinalBuilder withVerified(boolean verified) {
    this.verified = verified;
    return this;
  }

  @Override
  public FinalBuilder withRollingPeriod(int rollingPeriod) {
    this.rollingPeriod = rollingPeriod;
    return this;
  }

  @Override
  public FinalBuilder withConsentToFederation(boolean consentToFederation) {
    this.consentToFederation = consentToFederation;
    return this;
  }

  @Override
  public FinalBuilder withCountryCode(String countryCode) {
    this.countryCode = countryCode;
    return this;
  }

  @Override
  public FinalBuilder withVisitedCountries(Set<String> visitedCountries) {
    this.visitedCountries = visitedCountries;
    return this;
  }

  @Override
  public FinalBuilder withReportType(ReportType reportType) {
    this.reportType = reportType;
    return this;
  }

  @Override
  public FinalBuilder withDaysSinceOnsetOfSymptoms(Integer daysSinceOnsetOfSymptoms) {
    this.daysSinceOnsetOfSymptoms = daysSinceOnsetOfSymptoms;
    return this;
  }

  @Override
  public FinalBuilder withFieldNormalization(DiagnosisKeyNormalizer fieldNormalizer) {
    this.fieldNormalizer = fieldNormalizer;
    return this;
  }

  @Override
  public DiagnosisKey build() {
    if (submissionTimestamp == null) {
      // hours since epoch
      submissionTimestamp = Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
    }

    if (visitedCountries == null || visitedCountries.isEmpty()) {
      visitedCountries = new HashSet<>();
      visitedCountries.add(countryCode);
    }

    NormalizableFields normalizedValues = normalizeValues();

    var diagnosisKey = new DiagnosisKey(
        keyData,
        rollingStartIntervalNumber,
        rollingPeriod,
        transmissionRiskLevel,
        submissionTimestamp,
        mobileTestId,
        mobileTestId2,
        datePatientInfectious,
        dateTestCommunicated,
        resultChannel,
        consentToFederation,
        countryCode,
        enhanceVisitedCountriesWithOriginCountry(),
        reportType,
        normalizedValues.getDaysSinceOnsetOfSymptoms(),
        verified);

    return throwIfValidationFails(diagnosisKey);
  }

  private Set<String> enhanceVisitedCountriesWithOriginCountry() {
    Set<String> enhancedVisitedCountries = new HashSet<>();

    if (visitedCountries == null) {
      visitedCountries = new HashSet<>();
    }

    enhancedVisitedCountries.addAll(visitedCountries);
    enhancedVisitedCountries.add(countryCode);
    return enhancedVisitedCountries;
  }

  private DiagnosisKey throwIfValidationFails(DiagnosisKey diagnosisKey) {
    Set<ConstraintViolation<DiagnosisKey>> violations = diagnosisKey.validate();

    if (!violations.isEmpty()) {
      String violationsMessage = violations.stream()
          .map(violation -> String.format("%s Invalid Value: %s", violation.getMessage(), violation.getInvalidValue()))
          .collect(Collectors.toList()).toString();
      logger.debug(violationsMessage);
      throw new InvalidDiagnosisKeyException(violationsMessage);
    }

    return diagnosisKey;
  }

  /**
   * If a {@link DiagnosisKeyNormalizer} object was configured in this builder, apply normalization where possibile, and
   * return a container with the new values. Otherwise return a container with the original unchanged values. For boxed
   * types, primitive zero like values will be chosen if they have not been provided by the client of the builder.
   */
  private NormalizableFields normalizeValues() {
    if (fieldNormalizer != null) {
      return fieldNormalizer.normalize(NormalizableFields.of(transmissionRiskLevel, daysSinceOnsetOfSymptoms));
    }
    return NormalizableFields.of(Objects.isNull(transmissionRiskLevel) ? 0 : transmissionRiskLevel,
        Objects.isNull(daysSinceOnsetOfSymptoms) ? 0 : daysSinceOnsetOfSymptoms);
  }
}
