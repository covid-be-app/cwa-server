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

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.R1Calculator;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.util.CryptoUtils;
import app.coronawarn.server.services.submission.validation.ValidSubmissionPayload;
import io.micrometer.core.annotation.Timed;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/version/v1")
@Validated
public class SubmissionController {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
  /**
   * The route to the submission endpoint (version agnostic).
   */
  public static final String SUBMISSION_ROUTE = "/diagnosis-keys";
  public static final String EMPTY_SUFFIX = "";

  private final SubmissionMonitor submissionMonitor;
  private final DiagnosisKeyService diagnosisKeyService;
  private final Integer retentionDays;
  private final Integer randomKeyPaddingMultiplier;
  private final FakeDelayManager fakeDelayManager;

  SubmissionController(
      DiagnosisKeyService diagnosisKeyService, FakeDelayManager fakeDelayManager,
      SubmissionServiceConfig submissionServiceConfig, SubmissionMonitor submissionMonitor) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.submissionMonitor = submissionMonitor;
    this.fakeDelayManager = fakeDelayManager;
    retentionDays = submissionServiceConfig.getRetentionDays();
    randomKeyPaddingMultiplier = submissionServiceConfig.getRandomKeyPaddingMultiplier();
  }

  /**
   * Handles diagnosis key submission requests.
   *
   * @param exposureKeys The unmarshalled protocol buffers submission payload.
   * @return An empty response body.
   */
  @PostMapping(value = SUBMISSION_ROUTE)
  @Timed(description = "Time spent handling submission.")
  public DeferredResult<ResponseEntity<Void>> submitDiagnosisKey(
      @ValidSubmissionPayload @RequestBody SubmissionPayload exposureKeys,
      @RequestHeader("Secret-Key") String secretKey,
      @RequestHeader("Random-String") String randomString,
      @RequestHeader("Result-Channel") Integer resultChannel,
      @DateTimeFormat(iso = ISO.DATE) @RequestHeader("Date-Patient-Infectious") LocalDate datePatientInfectious,
      @DateTimeFormat(iso = ISO.DATE) @RequestHeader("Date-Test-Communicated") LocalDate dateTestCommunicated) {

    return buildRealDeferredResult(exposureKeys,secretKey,randomString,datePatientInfectious,dateTestCommunicated,
        resultChannel);
  }

  private DeferredResult<ResponseEntity<Void>> buildRealDeferredResult(SubmissionPayload exposureKeys,
      String secretKey, String randomString, LocalDate datePatientInfectious, LocalDate dateTestCommunicated,
      Integer resultChannel) {
    DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {

      logger.debug("Found Secret-Key = " + secretKey);
      logger.debug("Found Random-String = " + randomString);
      logger.debug("Found Date-Patient-Infectious = " + datePatientInfectious);
      logger.debug("Found Date-Test-Communicated = " + dateTestCommunicated);
      logger.debug("Found Result-Channel = " + resultChannel);

      R1Calculator r1Calculator = new R1Calculator(datePatientInfectious,
          randomString,
          CryptoUtils.TEXT,
          CryptoUtils.decodeAesKey(secretKey));

      String mobileTestId = r1Calculator.generate15Digits();

      R1Calculator r1AlternateCalculator = new R1Calculator(datePatientInfectious,
          randomString,
          EMPTY_SUFFIX, // Needed to generate the R1 as the android does at the time of writing.
          CryptoUtils.decodeAesKey(secretKey));

      String mobileTestId2 = r1AlternateCalculator.generate15Digits();

      persistDiagnosisKeysPayload(
          exposureKeys,
          mobileTestId,
          mobileTestId2,
          datePatientInfectious,
          dateTestCommunicated,
          resultChannel);

      deferredResult.setResult(ResponseEntity.ok().build());


    } catch (Exception e) {
      deferredResult.setErrorResult(e);
    } finally {
      stopWatch.stop();
      fakeDelayManager.updateFakeRequestDelay(stopWatch.getTotalTimeMillis());
    }

    return deferredResult;
  }

  /**
   * Persists the diagnosis keys contained in the specified request payload.
   *
   * @param protoBufDiagnosisKeys Diagnosis keys that were specified in the request.
   * @param mobileTestId The mobile test id
   * @param mobileTestId2 The mobile test id (as currently generated by android at the time of writing)
   * @param datePatientInfectious The date patient was infectious
   * @param dateTestCommunicated The date the test was communicated
   * @param resultChannel the test result channel
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void persistDiagnosisKeysPayload(SubmissionPayload protoBufDiagnosisKeys,
      String mobileTestId, String mobileTestId2,
      LocalDate datePatientInfectious, LocalDate dateTestCommunicated, Integer resultChannel) {

    List<TemporaryExposureKey> protoBufferKeysList = protoBufDiagnosisKeys.getKeysList();
    List<DiagnosisKey> diagnosisKeys = new ArrayList<>();

    int i = 0;

    for (TemporaryExposureKey protoBufferKey : protoBufferKeysList) {
      DiagnosisKey diagnosisKey = DiagnosisKey
          .builder()
          .fromProtoBuf(protoBufferKey)
          .withCountry(protoBufDiagnosisKeys.getCountries(i))
          .withMobileTestId(mobileTestId)
          .withMobileTestId2(mobileTestId2)
          .withDatePatientInfectious(datePatientInfectious)
          .withDateTestCommunicated(dateTestCommunicated)
          .withResultChannel(resultChannel)
          .withVerified(false)
          .build();

      if (diagnosisKey.isYoungerThanRetentionThreshold(retentionDays)) {
        diagnosisKeys.add(diagnosisKey);
      } else {
        logger.info("Not persisting a diagnosis key, as it is outdated beyond retention threshold.");
      }

      submissionMonitor.incrementRequestCounter();

      i++;
    }



    diagnosisKeyService.saveDiagnosisKeys(padDiagnosisKeys(diagnosisKeys));
  }

  private List<DiagnosisKey> padDiagnosisKeys(List<DiagnosisKey> diagnosisKeys) {
    List<DiagnosisKey> paddedDiagnosisKeys = new ArrayList<>();
    diagnosisKeys.forEach(diagnosisKey -> {
      paddedDiagnosisKeys.add(diagnosisKey);
      IntStream.range(1, randomKeyPaddingMultiplier)
          .mapToObj(index -> DiagnosisKey.builder()
              .withKeyData(generateRandomKeyData())
              .withRollingStartIntervalNumber(diagnosisKey.getRollingStartIntervalNumber())
              .withTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel())
              .withRollingPeriod(diagnosisKey.getRollingPeriod())
              .withCountry(diagnosisKey.getCountry())
              .withMobileTestId(diagnosisKey.getMobileTestId())
              .withMobileTestId2(diagnosisKey.getMobileTestId2())
              .withDatePatientInfectious(diagnosisKey.getDatePatientInfectious())
              .withDateTestCommunicated(diagnosisKey.getDateTestCommunicated())
              .withResultChannel(diagnosisKey.getResultChannel())
              .build())
          .forEach(paddedDiagnosisKeys::add);
    });
    return paddedDiagnosisKeys;
  }

  private static byte[] generateRandomKeyData() {
    byte[] randomKeyData = new byte[16];
    new SecureRandom().nextBytes(randomKeyData);
    return randomKeyData;
  }


}
