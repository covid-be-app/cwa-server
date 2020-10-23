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

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import java.time.Instant;
import java.time.LocalDate;
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
  private String country;
  private String mobileTestId;
  private String mobileTestId2;
  private LocalDate datePatientInfectious;
  private LocalDate dateTestCommunicated;
  private int resultChannel;
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
  public FinalBuilder withTransmissionRiskLevel(int transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  @Override
  public FinalBuilder fromProtoBuf(TemporaryExposureKey protoBufObject) {
    return this
        .withKeyData(protoBufObject.getKeyData().toByteArray())
        .withRollingStartIntervalNumber(protoBufObject.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(performCorrectionOnTransmissionRiskLevel(protoBufObject))
        .withRollingPeriod(protoBufObject.getRollingPeriod());
  }

  private int performCorrectionOnTransmissionRiskLevel(TemporaryExposureKey protoBufObject) {
    return protoBufObject.getTransmissionRiskLevel() == INVALID_MAX_TRANSMISSION_RISK_LEVEL
        ? MAX_TRANSMISSION_RISK_LEVEL : protoBufObject.getTransmissionRiskLevel();
  }

  @Override
  public FinalBuilder withSubmissionTimestamp(long submissionTimestamp) {
    this.submissionTimestamp = submissionTimestamp;
    return this;
  }

  @Override
  public FinalBuilder withCountry(String country) {
    this.country = country;
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
  public DiagnosisKey build() {
    if (submissionTimestamp == null) {
      // hours since epoch
      submissionTimestamp = Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
    }

    var diagnosisKey = new DiagnosisKey(
        keyData,
        rollingStartIntervalNumber,
        rollingPeriod,
        transmissionRiskLevel,
        submissionTimestamp,
        country,
        mobileTestId,
        mobileTestId2,
        datePatientInfectious,
        dateTestCommunicated,
        resultChannel,
        verified);

    return throwIfValidationFails(diagnosisKey);
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
}
