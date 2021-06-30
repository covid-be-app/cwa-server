package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.HttpHeaderBuilder.COVICODE;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_2;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_3;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.createRollingStartIntervalNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.covicodes.CoviCode;
import app.coronawarn.server.common.persistence.repository.CoviCodeRepository;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "services.submission.verification.rate=1000"
})
@ActiveProfiles({"disable-ssl-client-verification",
    "disable-ssl-client-verification-verify-hostname"})
public class CoviCodeMetricsCountTest {

  @Autowired
  private SubmissionServiceConfig config;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @MockBean
  private SubmissionMonitor submissionMonitor;

  @MockBean
  private CoviCodeRepository coviCodeRepository;

  @BeforeEach
  public void before() {
    diagnosisKeyRepository.deleteAll();
  }

  @Test
  public void postKeysWithValidCoviCode() {

    when(coviCodeRepository.getUnusedCoviCode(COVICODE)).thenReturn(Optional.of(CoviCode.builder()
        .withCode(COVICODE)
        .withStartInterval(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5))
        .withEndInterval(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5))
        .build()));

    ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys()), buildHeaderWithCoviCode());
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
    verify(submissionMonitor, times(1)).incrementValidCoviCodeCounter();
    verify(submissionMonitor, times(0)).incrementInvalidCoviCodeCounter();
  }

  @Test
  public void postKeysWithInvalidCoviCode() {

    when(coviCodeRepository.getUnusedCoviCode(COVICODE)).thenReturn(Optional.of(CoviCode.builder()
        .withCode(COVICODE)
        .withStartInterval(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(60))
        .withEndInterval(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(55))
        .build()));

    ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys()), buildHeaderWithCoviCode());
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);
    verify(submissionMonitor, times(0)).incrementValidCoviCodeCounter();
    verify(submissionMonitor, times(1)).incrementInvalidCoviCodeCounter();

  }

  @Test
  public void postKeysWithInvalidUnknownCoviCode() {

    when(coviCodeRepository.getUnusedCoviCode(COVICODE)).thenReturn(Optional.empty());

    ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys()), buildHeaderWithCoviCode());
    assertThat(actResponse.getStatusCode()).isEqualTo(FORBIDDEN);
    verify(submissionMonitor, times(0)).incrementValidCoviCodeCounter();
    verify(submissionMonitor, times(1)).incrementInvalidCoviCodeCounter();

  }

  @Test
  public void postKeysWithoutCoviCode() {
    ResponseEntity<Void> actResponse = executor.executePost(buildPayload(buildMultipleKeys()));
    assertThat(actResponse.getStatusCode()).isEqualTo(OK);
    verify(submissionMonitor, times(0)).incrementValidCoviCodeCounter();
    verify(submissionMonitor, times(0)).incrementInvalidCoviCodeCounter();
  }

  private HttpHeaders buildHeaderWithCoviCode() {
    return HttpHeaderBuilder.builder()
        .contentTypeProtoBuf()
        .randomString(HttpHeaderBuilder.RANDOM_STRING)
        .secretKey(HttpHeaderBuilder.SECRET_KEY)
        .datePatientInfectious(HttpHeaderBuilder.DATE_PATIENT_INFECTUOUS)
        .dateTestCommunicated()
        .dateOnsetOfSymptoms()
        .resultChannel("3")
        .coviCode()
        .build();
  }


  private SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys) {

    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(buildCountries(keys.size()))
        .build();
  }

  private Collection<String> buildCountries(int size) {
    String[] countries = new String[size];
    Arrays.setAll(countries, c -> "BE");
    return Stream.of(countries)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private Collection<TemporaryExposureKey> buildMultipleKeys() {
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(config.getRetentionDays() - 1);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.EXPECTED_ROLLING_PERIOD;
    return Stream.of(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 3),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber3, 6),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber2, 8))
        .collect(Collectors.toCollection(ArrayList::new));
  }

}
