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

import static app.coronawarn.server.common.persistence.domain.DiagnosisKey.MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS;
import static app.coronawarn.server.common.persistence.domain.DiagnosisKey.MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.covicodes.CoviCode;
import app.coronawarn.server.common.persistence.repository.CoviCodeRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.persistence.utils.CryptoUtils;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.R1Calculator;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.normalization.SubmissionKeyNormalizer;
import app.coronawarn.server.services.submission.validation.ValidSubmissionPayload;
import io.micrometer.core.annotation.Timed;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
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
  public static final int CALL_CENTER = 3;

  private final SubmissionMonitor submissionMonitor;
  private final DiagnosisKeyService diagnosisKeyService;
  private final Integer retentionDays;
  private final Integer randomKeyPaddingMultiplier;
  private final FakeDelayManager fakeDelayManager;
  private final SubmissionServiceConfig submissionServiceConfig;
  private final CoviCodeRepository coviCodeRepository;


  SubmissionController(
      DiagnosisKeyService diagnosisKeyService, FakeDelayManager fakeDelayManager,
      SubmissionServiceConfig submissionServiceConfig, SubmissionMonitor submissionMonitor,
      CoviCodeRepository coviCodeRepository) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.submissionMonitor = submissionMonitor;
    this.fakeDelayManager = fakeDelayManager;
    this.submissionServiceConfig = submissionServiceConfig;
    this.coviCodeRepository = coviCodeRepository;
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
      @DateTimeFormat(iso = ISO.DATE) @RequestHeader("Date-Test-Communicated") LocalDate dateTestCommunicated,
      @DateTimeFormat(iso = ISO.DATE)
      @RequestHeader(value = "Date-Onset-Of-Symptoms", required = false) LocalDate dateOnsetOfSymptoms,
      @RequestHeader(value = "Covi-Code", required = false) String coviCode) {

    return buildRealDeferredResult(exposureKeys, secretKey, randomString,
        datePatientInfectious, dateTestCommunicated, dateOnsetOfSymptoms,
        resultChannel, coviCode);
  }

  private DeferredResult<ResponseEntity<Void>> buildRealDeferredResult(SubmissionPayload submissionPayload,
      String secretKey, String randomString, LocalDate datePatientInfectious, LocalDate dateTestCommunicated,
      LocalDate dateOnsetOfSymptoms, Integer resultChannel, String coviCode) {
    DeferredResult<ResponseEntity<Void>> deferredResult = new DeferredResult<>();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {

      logger.debug("Found Secret-Key = " + secretKey);
      logger.debug("Found Random-String = " + randomString);
      logger.debug("Found Date-Patient-Infectious = " + datePatientInfectious);
      logger.debug("Found Date-Test-Communicated = " + dateTestCommunicated);
      logger.debug("Found Date-Onset-Of-Symptoms = " + dateOnsetOfSymptoms);
      logger.info("Found Result-Channel = " + resultChannel);
      logger.info("Found Covi-Code = " + coviCode);

      if (resultChannel == CALL_CENTER && !StringUtils.isEmpty(coviCode)) {

        logger.info("Submission via callcenter with CoviCode {}",coviCode);

        coviCodeRepository.getUnusedCoviCode(coviCode).filter(CoviCode::isValid)
            .orElseThrow(InvalidCoviCodeException::new);

        logger.info("Valid CoviCode {} found,Incrementing valid coviCode counter",coviCode);

        submissionMonitor.incrementValidCoviCodeCounter();

        persistDiagnosisKeysPayload(
            submissionPayload,
            "000000000000000",
            "000000000000000",
            datePatientInfectious,
            dateTestCommunicated,
            dateOnsetOfSymptoms,
            CALL_CENTER,
            true); // with a valid covicode, the keys are automatically verified

        logger.info("Marking coviCode {} as used",coviCode);
        coviCodeRepository.coviCodeUsed(coviCode);

      } else {

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
            submissionPayload,
            mobileTestId,
            mobileTestId2,
            datePatientInfectious,
            dateTestCommunicated,
            dateOnsetOfSymptoms,
            resultChannel,
            false); // wait for the authorization code verification process

      }

      deferredResult.setResult(ResponseEntity.ok().build());


    } catch (InvalidCoviCodeException e) {
      logger.info("Invalid coviCode {} submitted. Incrementing invalid counter",coviCode);
      submissionMonitor.incrementInvalidCoviCodeCounter();
      deferredResult.setResult(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
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
   * @param submissionPayload     Diagnosis keys that were specified in the request.
   * @param mobileTestId          The mobile test id
   * @param mobileTestId2         The mobile test id (as currently generated by android at the time of writing)
   * @param datePatientInfectious The date patient was infectious
   * @param dateTestCommunicated  The date the test was communicated
   * @param resultChannel         the test result channel
   * @throws IllegalArgumentException in case the given collection contains {@literal null}.
   */
  public void persistDiagnosisKeysPayload(SubmissionPayload submissionPayload,
      String mobileTestId, String mobileTestId2,
      LocalDate datePatientInfectious, LocalDate dateTestCommunicated, LocalDate dateOnsetOfSymptoms,
      Integer resultChannel, boolean verified) {

    List<DiagnosisKey> diagnosisKeys = extractValidDiagnosisKeysFromPayload(
        enhanceWithDefaultValuesIfMissing(submissionPayload),
        mobileTestId,
        mobileTestId2,
        datePatientInfectious,
        dateTestCommunicated,
        dateOnsetOfSymptoms,
        resultChannel,
        verified);

    //checkDiagnosisKeysStructure(diagnosisKeys);

    diagnosisKeyService.saveDiagnosisKeys(padDiagnosisKeys(diagnosisKeys));
  }

  private List<DiagnosisKey> extractValidDiagnosisKeysFromPayload(SubmissionPayload submissionPayload,
      String mobileTestId, String mobileTestId2,
      LocalDate datePatientInfectious, LocalDate dateTestCommunicated, LocalDate dateOnsetOfSymptoms,
      Integer resultChannel, boolean verified) {

    List<TemporaryExposureKey> protoBufferKeys = submissionPayload.getKeysList();

    List<DiagnosisKey> diagnosisKeys = protoBufferKeys.stream()
        .map(protoBufferKey -> DiagnosisKey.builder()
            .fromTemporaryExposureKeyAndMetadata(
                protoBufferKey,
                submissionPayload.getVisitedCountriesList(),
                submissionPayload.getOrigin(),
                submissionPayload.getConsentToFederation())
            .withFieldNormalization(new SubmissionKeyNormalizer(submissionServiceConfig, dateOnsetOfSymptoms))
            .withDaysSinceOnsetOfSymptoms(daysBetweenRollingAndDayOfSymptoms(protoBufferKey, dateOnsetOfSymptoms))
            .withMobileTestId(mobileTestId)
            .withMobileTestId2(mobileTestId2)
            .withDatePatientInfectious(datePatientInfectious)
            .withDateTestCommunicated(dateTestCommunicated)
            .withResultChannel(resultChannel)
            .withVerified(verified)
            .build()
        )
        .filter(diagnosisKey -> diagnosisKey.isYoungerThanRetentionThreshold(retentionDays))
        .collect(Collectors.toList());

    // TODO: check correct place ?
    diagnosisKeys.forEach(dk -> submissionMonitor.incrementRequestCounter());

    if (protoBufferKeys.size() > diagnosisKeys.size()) {
      logger.warn("Not persisting {} diagnosis key(s), as it is outdated beyond retention threshold.",
          protoBufferKeys.size() - diagnosisKeys.size());
    }
    return diagnosisKeys;
  }

  /**
   * Checks if a key with transmission risk level 6 is missing in the submitted diagnosis keys. If there is one, it
   * should not have a rolling start interval number of today midnight. In case of violations, these are logged.
   *
   * <p>The check is only done for the key with transmission risk level 6, since the number of keys to be submitted
   * depends on the time how long the app is installed on the phone. The key with transmission risk level 6 is the one
   * from the day before the submission and should always be present.
   *
   * @param diagnosisKeys The diagnosis keys to check.
   */
  private void checkDiagnosisKeysStructure(List<DiagnosisKey> diagnosisKeys) {
    diagnosisKeys.sort(Comparator.comparing(DiagnosisKey::getRollingStartIntervalNumber));
    String keysString = Arrays.toString(diagnosisKeys.toArray());
    Predicate<DiagnosisKey> hasRiskLevel6 = diagnosisKey -> diagnosisKey.getTransmissionRiskLevel() == 6;

    if (diagnosisKeys.stream().noneMatch(hasRiskLevel6)) {
      logger.warn("Submission payload was sent with missing key having transmission risk level 6. {}", keysString);
    } else {
      logger.debug("Submission payload was sent with key having transmission risk level 6. {}", keysString);
    }

    diagnosisKeys.stream().filter(hasRiskLevel6).findFirst().ifPresent(diagnosisKey -> {
      long todayMidnightUtc = LocalDate
          .ofInstant(Instant.now(), UTC)
          .atStartOfDay()
          .toEpochSecond(UTC) / (60 * 10);
      if (diagnosisKey.getRollingStartIntervalNumber() == todayMidnightUtc) {
        logger.warn("Submission payload was sent with a key having transmission risk level 6"
            + " and rolling start interval number of today midnight. {}", keysString);
      }
    });
  }

  private LocalDate convertRollingStartIntervalToDate(int rollingStartIntervalNumber) {
    return Instant.ofEpochSecond(rollingStartIntervalNumber * (60 * 10)).atZone(UTC).toLocalDate();
  }

  private Integer daysBetweenRollingAndDayOfSymptoms(TemporaryExposureKey temporaryExposureKey,
      LocalDate dateOnsetOfSymptoms) {

    if (temporaryExposureKey.hasDaysSinceOnsetOfSymptoms()) {
      return temporaryExposureKey.getDaysSinceOnsetOfSymptoms();
    }

    if (dateOnsetOfSymptoms == null) {
      return null;
    }

    LocalDate rollingStartIntervalDate =
        convertRollingStartIntervalToDate(temporaryExposureKey.getRollingStartIntervalNumber());

    Integer result = Long.valueOf(
        ChronoUnit.DAYS.between(dateOnsetOfSymptoms, rollingStartIntervalDate))
        .intValue();

    if (result < MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS) {
      return MIN_DAYS_SINCE_ONSET_OF_SYMPTOMS;
    }

    if (result > MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS) {
      return MAX_DAYS_SINCE_ONSET_OF_SYMPTOMS;
    }

    return result;
  }

  private SubmissionPayload enhanceWithDefaultValuesIfMissing(SubmissionPayload submissionPayload) {
    String originCountry = defaultIfEmptyOriginCountry(submissionPayload.getOrigin());

    return SubmissionPayload.newBuilder()
        .addAllKeys(submissionPayload.getKeysList())
        .setRequestPadding(submissionPayload.getRequestPadding())
        .addAllVisitedCountries(Arrays.asList(submissionServiceConfig.getSupportedCountries()))
        .setOrigin(originCountry)
        .setConsentToFederation(submissionPayload.getConsentToFederation())
        .build();
  }

  private String defaultIfEmptyOriginCountry(String originCountry) {
    return StringUtils.defaultIfBlank(originCountry, submissionServiceConfig.getDefaultOriginCountry());
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
              .withVisitedCountries(diagnosisKey.getVisitedCountries())
              .withCountryCode(diagnosisKey.getOriginCountry())
              .withReportType(diagnosisKey.getReportType())
              .withConsentToFederation(diagnosisKey.isConsentToFederation())
              .withDaysSinceOnsetOfSymptoms(diagnosisKey.getDaysSinceOnsetOfSymptoms())
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
