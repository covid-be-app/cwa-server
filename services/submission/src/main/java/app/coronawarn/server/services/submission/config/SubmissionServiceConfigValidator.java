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

package app.coronawarn.server.services.submission.config;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * {@link SubmissionPayloadSizeFilter} instances validate the values of the SubmissionServiceConfig.
 */
public class SubmissionServiceConfigValidator implements Validator {

  public static final DataSize MIN_MAXIMUM_REQUEST_SIZE = DataSize.ofBytes(280);
  public static final DataSize MAX_MAXIMUM_REQUEST_SIZE = DataSize.ofKilobytes(200);
  private static final List<String> ISO_COUNTRIES = Arrays.asList(Locale.getISOCountries());

  @Override
  public boolean supports(Class<?> type) {
    return type == SubmissionServiceConfig.class;
  }

  /**
   * Validates the following constraints.
   * <ul>
   *   <li>MaximumRequestSize is in the defined range.</li>
   *   <li>List of SupportedCountries contains only valid ISO Codes</li>
   *   <li>Mapping of trl (Transmission Risk Level) to dsos contains only values in allowed range</li>
   *   <li>Mapping of dsos (Days Since Onset of Symptoms) to trl contains only values in allowed range</li>
   * </ul>
   */
  @Override
  public void validate(Object o, Errors errors) {
    SubmissionServiceConfig properties = (SubmissionServiceConfig) o;

    validateMaxRequestSize(errors, properties);
    validateSupportedCountries(errors, properties);
    validateDaysSinceSymptomsDerivationMap(errors, properties);
    validateTransmissionRiskLevelDerivationMap(errors, properties);
  }

  private void validateTransmissionRiskLevelDerivationMap(Errors errors,
      SubmissionServiceConfig properties) {
    properties.getTekFieldDerivations().getTransmissionRiskLevelFromDaysSinceSymptoms()
        .forEach((daysSinceOnsetOfSymptoms, transmissionRiskLevel) -> {
          checkTrlInAllowedRange(transmissionRiskLevel, errors);
          checkDsosInAllowedRange(daysSinceOnsetOfSymptoms, errors);
        });
  }

  private void validateDaysSinceSymptomsDerivationMap(Errors errors,
      SubmissionServiceConfig properties) {
    properties.getTekFieldDerivations().getDaysSinceSymptomsFromTransmissionRiskLevel()
        .forEach((transmissionRiskLevel, daysSinceOnsetOfSymptoms) -> {
          checkTrlInAllowedRange(transmissionRiskLevel, errors);
          checkDsosInAllowedRange(daysSinceOnsetOfSymptoms, errors);
        });
  }

  private void checkTrlInAllowedRange(Integer transmissionRiskLevel, Errors errors) {
    if (transmissionRiskLevel > DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL
        || transmissionRiskLevel < DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL) {
      errors.rejectValue("tekFieldDerivations",
          "[" + transmissionRiskLevel + "]: transmissionRiskLevel value is not in the allowed range "
              + "(" + DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL + " to "
              + DiagnosisKey.MAX_TRANSMISSION_RISK_LEVEL + ")");
    }
  }

  private void checkDsosInAllowedRange(Integer daysSinceOnsetSymptoms, Errors errors) {
    if (daysSinceOnsetSymptoms > DiagnosisKey.MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS
        || daysSinceOnsetSymptoms < DiagnosisKey.MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS) {
      errors.rejectValue("tekFieldDerivations",
          "[" + daysSinceOnsetSymptoms + "]: daysSinceOnsetSymptoms value is not in the allowed range "
              + "( " + DiagnosisKey.MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS + " to "
              + DiagnosisKey.MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS + ")");
    }
  }

  private void validateSupportedCountries(Errors errors, SubmissionServiceConfig properties) {
    Arrays.stream(properties.getSupportedCountries()).forEach(country -> {
      if (!ISO_COUNTRIES.contains(country) && !isBelgium3LetterIsoCode(country)) {
        errors.rejectValue("supportedCountries",
            "[" + country + "]: Country code isn't compliant to ISO 3166.");
      }
    });
  }

  private boolean isBelgium3LetterIsoCode(String country) {
    return "BEL".equals(country);
  }

  private void validateMaxRequestSize(Errors errors, SubmissionServiceConfig properties) {
    if (properties.getMaximumRequestSize().compareTo(MIN_MAXIMUM_REQUEST_SIZE) < 0
        || properties.getMaximumRequestSize().compareTo(MAX_MAXIMUM_REQUEST_SIZE) > 0) {
      errors.rejectValue("maximumRequestSize",
          "Must be at least " + MIN_MAXIMUM_REQUEST_SIZE + " and not more than " + MAX_MAXIMUM_REQUEST_SIZE + ".");
    }
  }
}
