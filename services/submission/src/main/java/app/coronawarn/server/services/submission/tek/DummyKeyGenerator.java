package app.coronawarn.server.services.submission.tek;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dummy-tek-generation")
public class DummyKeyGenerator {

  private static final Logger logger = LoggerFactory.getLogger(DummyKeyGenerator.class);

  private final DiagnosisKeyService diagnosisKeyService;
  private final SubmissionServiceConfig submissionServiceConfig;
  private final SubmissionMonitor submissionMonitor;


  /**
   * Creates the dummy key generator.
   */
  public DummyKeyGenerator(DiagnosisKeyService diagnosisKeyService, SubmissionServiceConfig submissionServiceConfig,
      SubmissionMonitor submissionMonitor) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.submissionServiceConfig = submissionServiceConfig;
    this.submissionMonitor = submissionMonitor;
  }

  /**
   * Generate dummy keys eligable for distribution.
   */
  @Scheduled(fixedDelayString = "${services.submission.tek.dummy.rate}")
  @Transactional
  public void generateDummyKeysForDistribution() throws Exception {

    int min = submissionServiceConfig.getTek().getDummy().getMinRange();
    int max = submissionServiceConfig.getTek().getDummy().getMaxRange();

    int nrOfKeysToAdd = new Random().nextInt(max - min + 1) + min;

    logger.info("Generating {} dummy keys", nrOfKeysToAdd);

    List<DiagnosisKey> diagnosisKeys = new ArrayList<>();

    for (int i = 0; i < nrOfKeysToAdd; i++) {
      DiagnosisKey diagnosisKey = DiagnosisKey
          .builder()
          .withKeyData(generateRandomKeyData())
          .withRollingStartIntervalNumber(createRollingStartIntervalNumber(1))
          .withTransmissionRiskLevel(1)
          .withRollingPeriod(144)
          .withCountryCode("BE")
          .withVisitedCountries(Set.of("BE","FR"))
          .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
          .withMobileTestId("000000000000000")
          .withDatePatientInfectious(LocalDate.now().minusDays(2))
          .withDateTestCommunicated(LocalDate.now())
          .withResultChannel(1)
          .withVerified(true)
          .build();

      diagnosisKeys.add(diagnosisKey);

    }

    diagnosisKeys.stream().forEach(k -> {
      submissionMonitor.incrementRequestCounter();
      submissionMonitor.incrementFakeRequestCounter();
    });

    diagnosisKeyService.saveDiagnosisKeys(diagnosisKeys);


  }

  private int createRollingStartIntervalNumber(Integer daysAgo) {
    return Math.toIntExact(LocalDate
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysAgo).atStartOfDay()
        .toEpochSecond(UTC) / (60 * 10));
  }

  private byte[] generateRandomKeyData() throws Exception {
    byte[] bytes = new byte[16];
    SecureRandom.getInstanceStrong().nextBytes(bytes);
    return bytes;
  }
}
