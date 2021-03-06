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

package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisKeyRepository extends PagingAndSortingRepository<DiagnosisKey, Long> {


  List<DiagnosisKey> findByVerified(Boolean verified);

  @Query("SELECT * FROM diagnosis_key WHERE (mobile_test_id =:mobileTestId OR mobile_test_id2 =:mobileTestId) "
      + "AND date_patient_infectious=:datePatientInfectious AND verified=:verified")
  List<DiagnosisKey> findByMobileTestIdOrMobileTestId2AndDatePatientInfectiousAndVerified(
      @Param("mobileTestId") String mobileTestId,
      @Param("datePatientInfectious") LocalDate datePatientInfectious,
      @Param("verified") boolean verified);

  /**
   * Counts all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be expired.
   * @return The number of expired keys.
   */
  @Query("SELECT COUNT(*) FROM diagnosis_key WHERE submission_timestamp<:threshold")
  int countOlderThan(@Param("threshold") long submissionTimestamp);

  /**
   * Deletes all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be deleted.
   */
  @Modifying
  @Query("DELETE FROM diagnosis_key WHERE submission_timestamp<:threshold")
  void deleteOlderThan(@Param("threshold") long submissionTimestamp);

  /**
   * Attempts to write the specified diagnosis key information into the database. If a row with the specified key data
   * already exists, no data is inserted.
   *
   * @param keyData                    The key data of the diagnosis key.
   * @param rollingStartIntervalNumber The rolling start interval number of the diagnosis key.
   * @param rollingPeriod              The rolling period of the diagnosis key.
   * @param submissionTimestamp        The submission timestamp of the diagnosis key.
   * @param transmissionRisk           The transmission risk level of the diagnosis key.
   * @param originCountry              The origin country from the app.
   * @param visitedCountries           The list of countries this transmissions is relevant for.
   * @param reportType                 The report type of the diagnosis key.
   * @param verified                   The verification status of the diagnosis key.
   * @return {@literal true} if the diagnosis key was inserted successfully, {@literal false} otherwise.
   *
   */
  @Modifying
  @Query("INSERT INTO diagnosis_key "
      + "(key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, "
      + "origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation, "
      + "mobile_test_id, mobile_test_id2, date_patient_infectious, date_test_communicated, result_channel, "
      + "verified) "
      + "VALUES (:keyData, :rollingStartIntervalNumber, :rollingPeriod, :submissionTimestamp, :transmissionRisk, "
      + ":origin_country, :visited_countries, :report_type, :days_since_onset_of_symptoms, :consent_to_federation, "
      + ":mobileTestId, :mobileTestId2 , :datePatientInfectious, :dateTestCommunicated, :resultChannel, "
      + " :verified) "
      + "ON CONFLICT DO NOTHING")
  boolean saveDoNothingOnConflict(
      @Param("keyData") byte[] keyData,
      @Param("rollingStartIntervalNumber") int rollingStartIntervalNumber,
      @Param("rollingPeriod") int rollingPeriod,
      @Param("submissionTimestamp") long submissionTimestamp,
      @Param("transmissionRisk") int transmissionRisk,
      @Param("mobileTestId") String mobileTestId,
      @Param("mobileTestId2") String mobileTestId2,
      @Param("datePatientInfectious") LocalDate datePatientInfectious,
      @Param("dateTestCommunicated") LocalDate dateTestCommunicated,
      @Param("resultChannel") int resultChannel,
      @Param("origin_country") String originCountry,
      @Param("visited_countries") String[] visitedCountries,
      @Param("report_type") String reportType,
      @Param("days_since_onset_of_symptoms") int daysSinceOnsetOfSymptoms,
      @Param("consent_to_federation") boolean consentToFederation,
      @Param("verified") boolean verified);
}


