package app.coronawarn.server.services.submission.authorizationcode;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.authorizationcode.AuthorizationCode;
import app.coronawarn.server.common.persistence.repository.AuthorizationCodeRepository;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.util.CryptoUtils;
import java.util.Iterator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This component will copy the ACs on an hourly basis to the submission server. That way the submission server can
 * validate incoming TEKs using the AC signature.
 */
@Component
public class AuthorizationCodeVerifier {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationCodeVerifier.class);

  private final AuthorizationCodeRepository authorizationCodeRepository;
  private final DiagnosisKeyRepository diagnosisKeyRepository;
  private final SubmissionServiceConfig submissionServiceConfig;
  private final CryptoUtils cryptoUtils;

  /**
   * Creates the authorization code verifier.
   */
  public AuthorizationCodeVerifier(AuthorizationCodeRepository authorizationCodeRepository,
      DiagnosisKeyRepository diagnosisKeyRepository, SubmissionServiceConfig submissionServiceConfig,
      CryptoUtils cryptoUtils) {
    this.authorizationCodeRepository = authorizationCodeRepository;
    this.diagnosisKeyRepository = diagnosisKeyRepository;
    this.submissionServiceConfig = submissionServiceConfig;
    this.cryptoUtils = cryptoUtils;
  }

  /**
   * Fetch all ACs and transfer them to the submission server.
   */
  @Scheduled(initialDelay = 2000, fixedDelayString = "${services.submission.verification.rate}")
  @Transactional
  public void verifyTekKeys() {

    Iterator<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository.findByVerified(false).iterator();

    diagnosisKeys.forEachRemaining(diagnosisKey -> {

      Optional<AuthorizationCode> authorizationCode = authorizationCodeRepository
          .findByMobileTestIdAndDatePatientInfectious(diagnosisKey.getMobileTestId(),
              diagnosisKey.getDatePatientInfectious());

      authorizationCode.ifPresent(ac -> {
        try {
          boolean verified = this.cryptoUtils.verify(diagnosisKey.getSignatureData(), ac.getSignature());
          diagnosisKey.setVerified(verified);
          diagnosisKeyRepository.save(diagnosisKey);
          logger.info("diagnosisKey = " + diagnosisKey + " = " + verified);
        } catch (Exception ex) {
          logger.error("Error occured during TEK key verification : " + ex.toString());
        }

      });

    });

  }

}
