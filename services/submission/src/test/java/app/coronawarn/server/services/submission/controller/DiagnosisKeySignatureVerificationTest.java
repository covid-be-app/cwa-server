package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.CornalertDataHolder.ANDROID_BACKEND_INTERPRETED_R1_PAYLOAD;
import static app.coronawarn.server.services.submission.controller.CornalertDataHolder.ANDROID_WRONG_R1_SIGNATURE_PAYLOAD;
import static app.coronawarn.server.services.submission.controller.CornalertDataHolder.INVALID_COMMUNICATION_DATE;
import static app.coronawarn.server.services.submission.controller.CornalertDataHolder.INVALID_INFECTIOUS_DATE;
import static app.coronawarn.server.services.submission.controller.CornalertDataHolder.INVALID_MOBILE_TEST_ID;
import static app.coronawarn.server.services.submission.controller.CornalertDataHolder.INVALID_SIGNATURE;
import static app.coronawarn.server.services.submission.controller.CornalertDataHolder.VALID_COMBO;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_2;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_3;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.createRollingStartIntervalNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.AuthorizationCodeRepository;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import rx.Single;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "services.submission.verification.rate=1000"
})
@ActiveProfiles({"ac-verification", "allow-authorizationcode-processing", "disable-ssl-client-verification",
    "disable-ssl-client-verification-verify-hostname"})
public class DiagnosisKeySignatureVerificationTest {

  @Autowired
  private AuthorizationCodeRepository authorizationCodeRepository;

  @Autowired
  private SubmissionServiceConfig config;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @MockBean
  private SubmissionMonitor submissionMonitor;

  @BeforeEach
  public void before() {
    authorizationCodeRepository.deleteAll();
    diagnosisKeyRepository.deleteAll();
  }


  /**
   * This test will persist a pre-defined signature (normally the signatures or ACs are sent to the submission server by
   * the test result server) for a particular mobile test id, date patient infectious and date patient communicated.
   * <p>
   * This is to setup our text fixture so that TEK uploads can be verified.
   * <p>
   * When we upload TEKs, we upload them with a specific random string, secret key and date patient infectious and date
   * patient. The test uses the following information
   * <p>
   * Secret-Key = +VhBgVyOB96AX1NHqEyibA== Random-String = uyVJlD1sfiSZkHDR Date-Patient-Infectious = 2020-08-27
   * Date-Test-Communicated = 2020-09-01 Result-Channel = 1
   * <p>
   * This will generate a mobileTestId (R1) = 945647857314342
   * <p>
   * When the test comes back positive and it is polled, it will generate a signature
   * <p>
   * 3045022100b521ba5a330002b323843eec30f6eab566a6b03ac6891e37266346315f194f7b022016ef96e916618084febdfbe6f24c4ebcd7ed9ea9474ea39102db84e61c13cbe6
   * <p>
   * That implies a valid combination of data in order to get to a verified TEK.
   * <p>
   * During the TEK upload, the mobileTestId is reconstructed from those fields, and the signature is looked up and
   * verified. Several things can happen :
   * <p>
   * For a given TEK upload the signature is not found for the given mobileTestId and as such TEK cannot be verified.
   * For a given TEK upload the signature is found but does not verify For a given TEK upload the signature is found and
   * is verified.
   */
  @Test
  @Disabled //TODO: check why this fails in CI/CD
  public void uploadTekWithValidCombo() {
    setupDataForCombo(VALID_COMBO, VALID_COMBO, true);
  }

  /**
   * Specific test for the invalid android R1 generation to validate that the server
   * will
   */
  @Test
  @Disabled //TODO: check why this fails in CI/CD
  public void uploadTekWithInvalidAndroidR1() throws Exception {
    setupDataForCombo(ANDROID_WRONG_R1_SIGNATURE_PAYLOAD, ANDROID_BACKEND_INTERPRETED_R1_PAYLOAD, true);
  }

  @Test
  public void uploadTekWithInvalidCommunicationDate() {
    setupDataForCombo(VALID_COMBO, INVALID_COMMUNICATION_DATE, false);
  }

  @Test
  public void uploadTekWithInvalidInfectiousDate() {
    setupDataForCombo(VALID_COMBO, INVALID_INFECTIOUS_DATE, false);
  }

  @Test
  public void uploadTekMobileTestIdNotFoundSoNotVerified() {
    setupDataForCombo(INVALID_MOBILE_TEST_ID, VALID_COMBO, false);
  }

  @Test
  public void uploadTekSignatureNotFoundSoNotVerified() {
    setupDataForCombo(INVALID_SIGNATURE, VALID_COMBO, false);
  }


  /**
   * Here we setup the data by inserting a valid authorization code into the system, based on test result poll. We then
   * upload a set of TEK keys with meta-data (coming from the cornalertDataHolder) and check if this will result in
   * verified TEKs
   *
   * @param tekUploadData
   */
  private void setupDataForCombo(CornalertDataHolder signatureData, CornalertDataHolder tekUploadData,
      Boolean expectedOutcome) {

    authorizationCodeRepository
        .saveDoNothingOnConflict(
            signatureData.getSignature(),
            signatureData.getMobileTestId(),
            signatureData.getDatePatientInfectious(),
            signatureData.getDateTestCommunicated()
        );

    ResponseEntity<Void> actResponse = executor.executePost(
        buildPayload(buildMultipleKeys()),
        tekUploadData.buildHeader()
    );

    assertThat(actResponse.getStatusCode()).isEqualTo(OK);

    Single.fromCallable(() -> true).delay(2, TimeUnit.SECONDS).toBlocking().value();

    List<DiagnosisKey> listFromIterator = getListFromIterator(
        diagnosisKeyRepository.findByVerified(expectedOutcome).iterator());

    Condition<DiagnosisKey> diagnosisKeys = new Condition<>(
        key -> expectedOutcome.equals(key.isVerified()), "diagnosisKeyVerificationResult");

    assertThat(listFromIterator).haveExactly(3, diagnosisKeys);

    verify(submissionMonitor, times(3)).incrementRequestCounter();

    if (expectedOutcome==true) {
      verify(submissionMonitor, times(1)).incrementAcVerified();
      verify(submissionMonitor, times(3)).incrementRealRequestCounter();
    } else {
      verify(submissionMonitor, times(0)).incrementAcVerified();
      verify(submissionMonitor, times(0)).incrementRealRequestCounter();
    }
  }

  private <T> List<T> getListFromIterator(Iterator<T> iterator) {
    List<T> list = new ArrayList<>();
    iterator.forEachRemaining(list::add);
    return list;
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
