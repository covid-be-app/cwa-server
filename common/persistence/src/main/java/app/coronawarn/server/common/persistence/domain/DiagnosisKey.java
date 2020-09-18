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

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilders.Builder;
import app.coronawarn.server.common.persistence.domain.validation.CountryIso3166;
import app.coronawarn.server.common.persistence.domain.validation.ValidRollingStartIntervalNumber;
import app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.Id;

/**
 * A key generated for advertising over a window of time.
 */
public class DiagnosisKey {

  /**
   * According to "Setting Up an Exposure Notification Server" by Apple, exposure notification servers are expected to
   * reject any diagnosis keys that do not have a rolling period of a certain fixed value. See
   * https://developer.apple.com/documentation/exposurenotification/setting_up_an_exposure_notification_server
   */
  public static final int EXPECTED_ROLLING_PERIOD = 144;

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  @Id
  @Size(min = 16, max = 16, message = "Key data must be a byte array of length 16.")
  private final byte[] keyData;

  @ValidRollingStartIntervalNumber
  private final int rollingStartIntervalNumber;

  @Range(min = EXPECTED_ROLLING_PERIOD, max = EXPECTED_ROLLING_PERIOD,
      message = "Rolling period must be " + EXPECTED_ROLLING_PERIOD + ".")
  private final int rollingPeriod;

  @Range(min = 0, max = 8, message = "Risk level must be between 0 and 8.")
  private final int transmissionRiskLevel;

  @ValidSubmissionTimestamp
  private final long submissionTimestamp;

  @CountryIso3166
  private String country;

  private String mobileTestId;

  private LocalDate datePatientInfectious;

  private LocalDate dateTestCommunicated;

  private int resultChannel;

  private boolean verified;

  /**
   * Should be called by builders.
   */
  DiagnosisKey(byte[] keyData, int rollingStartIntervalNumber, int rollingPeriod,
      int transmissionRiskLevel, long submissionTimestamp, String country,
      String mobileTestId, LocalDate datePatientInfectious, LocalDate dateTestCommunicated, int resultChannel,
      boolean verified) {
    this.keyData = keyData;
    this.rollingStartIntervalNumber = rollingStartIntervalNumber;
    this.rollingPeriod = rollingPeriod;
    this.transmissionRiskLevel = transmissionRiskLevel;
    this.submissionTimestamp = submissionTimestamp;
    this.country = country;
    this.mobileTestId = mobileTestId;
    this.datePatientInfectious = datePatientInfectious;
    this.dateTestCommunicated = dateTestCommunicated;
    this.resultChannel = resultChannel;
    this.verified = verified;
  }

  /**
   * Returns a DiagnosisKeyBuilder instance. A {@link DiagnosisKey} can then be build by either providing the required
   * member values or by passing the respective protocol buffer object.
   *
   * @return DiagnosisKeyBuilder instance.
   */
  public static Builder builder() {
    return new DiagnosisKeyBuilder();
  }

  /**
   * Returns the diagnosis key.
   */
  public byte[] getKeyData() {
    return keyData;
  }

  /**
   * Returns a number describing when a key starts. It is equal to startTimeOfKeySinceEpochInSecs / (60 * 10).
   */
  public int getRollingStartIntervalNumber() {
    return rollingStartIntervalNumber;
  }

  /**
   * Returns a number describing how long a key is valid. It is expressed in increments of 10 minutes (e.g. 144 for 24
   * hours).
   */
  public int getRollingPeriod() {
    return rollingPeriod;
  }

  /**
   * Returns the risk of transmission associated with the person this key came from.
   */
  public int getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  /**
   * Returns the timestamp associated with the submission of this {@link DiagnosisKey} as hours since epoch.
   */
  public long getSubmissionTimestamp() {
    return submissionTimestamp;
  }

  /**
   * Returns the country associated with this {@link DiagnosisKey}.
   */
  public String getCountry() {
    return country;
  }

  /**
   * Returns the mobileTestId associated with this {@link DiagnosisKey}.
   */
  public String getMobileTestId() {
    return mobileTestId;
  }

  /**
   * Returns the date when the patient was infectious associated with this {@link DiagnosisKey}.
   */
  public LocalDate getDatePatientInfectious() {
    return datePatientInfectious;
  }

  /**
   * Returns the date when the patient test was communicated associated with this {@link DiagnosisKey}.
   */
  public LocalDate getDateTestCommunicated() {
    return dateTestCommunicated;
  }

  /**
   * Returns the test result channel associated with this {@link DiagnosisKey}.
   */
  public int getResultChannel() {
    return resultChannel;
  }

  /**
   * Returns the verification status.
   */
  public boolean isVerified() {
    return verified;
  }

  /**
   * Allows us to set the verification state.
   */
  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  /**
   * Checks if this diagnosis key falls into the period between now, and the retention threshold.
   *
   * @param daysToRetain the number of days before a key is outdated
   * @return true, if the rolling start interval number is within the time between now, and the given days to retain
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  public boolean isYoungerThanRetentionThreshold(int daysToRetain) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Retention threshold must be greater or equal to 0.");
    }
    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysToRetain)
        .toEpochSecond(UTC) / (60 * 10);

    return this.rollingStartIntervalNumber >= threshold;
  }

  /**
   * Gets any constraint violations that this key might incorporate.
   *
   * <p><ul>
   * <li>Risk level must be between 0 and 8
   * <li>Rolling start interval number must be greater than 0
   * <li>Rolling start number cannot be in the future
   * <li>Rolling period must be positive number
   * <li>Key data must be byte array of length 16
   * </ul>
   *
   * @return A set of constraint violations of this key.
   */
  public Set<ConstraintViolation<DiagnosisKey>> validate() {
    return VALIDATOR.validate(this);
  }


  /**
   * The signature data contained in this diagnosis key that we will use to generate an AC.
   *
   * @return
   */
  public String getSignatureData() {
    StringBuilder sb = new StringBuilder();

    return sb
        .append(getMobileTestId())
        .append(getDatePatientInfectious())
        .append(getDateTestCommunicated())
        .append(getResultChannel())
        .toString();

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiagnosisKey that = (DiagnosisKey) o;
    return rollingStartIntervalNumber == that.rollingStartIntervalNumber
        && rollingPeriod == that.rollingPeriod
        && transmissionRiskLevel == that.transmissionRiskLevel
        && submissionTimestamp == that.submissionTimestamp
        && resultChannel == that.resultChannel
        && Arrays.equals(keyData, that.keyData)
        && Objects.equals(country, that.country)
        && Objects.equals(mobileTestId, that.mobileTestId)
        && Objects.equals(datePatientInfectious, that.datePatientInfectious)
        && Objects.equals(dateTestCommunicated, that.dateTestCommunicated);
  }

  @Override
  public int hashCode() {
    int result = Objects
        .hash(rollingStartIntervalNumber,
            rollingPeriod,
            transmissionRiskLevel,
            submissionTimestamp,
            country,
            mobileTestId,
            datePatientInfectious,
            dateTestCommunicated,
            resultChannel);
    result = 31 * result + Arrays.hashCode(keyData);
    return result;
  }

}
