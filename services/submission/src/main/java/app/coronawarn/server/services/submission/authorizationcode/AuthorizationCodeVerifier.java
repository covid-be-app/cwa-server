/*
 * Coronalert / cwa-server
 *
 * (c) 2020 Devside SRL
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.submission.authorizationcode;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.authorizationcode.AuthorizationCode;
import app.coronawarn.server.common.persistence.repository.AuthorizationCodeRepository;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import app.coronawarn.server.services.submission.util.CryptoUtils;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This component will verify all the TEKs based on the AC that it received previously. A verified flag will be set on
 * the TEKs and only those TEKs will be copied to the CDN.
 */
@Component
@Profile("ac-verification")
public class AuthorizationCodeVerifier {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationCodeVerifier.class);

  private final AuthorizationCodeRepository authorizationCodeRepository;
  private final DiagnosisKeyRepository diagnosisKeyRepository;
  private final SubmissionServiceConfig submissionServiceConfig;
  private final CryptoUtils cryptoUtils;
  private final SubmissionMonitor submissionMonitor;


  /**
   * Creates the authorization code verifier.
   */
  public AuthorizationCodeVerifier(AuthorizationCodeRepository authorizationCodeRepository,
      DiagnosisKeyRepository diagnosisKeyRepository, SubmissionServiceConfig submissionServiceConfig,
      CryptoUtils cryptoUtils,SubmissionMonitor submissionMonitor) {
    this.authorizationCodeRepository = authorizationCodeRepository;
    this.diagnosisKeyRepository = diagnosisKeyRepository;
    this.submissionServiceConfig = submissionServiceConfig;
    this.cryptoUtils = cryptoUtils;
    this.submissionMonitor = submissionMonitor;
  }

  /**
   * Fetch all ACs and transfer them to the submission server.
   */
  @Scheduled(fixedDelayString = "${services.submission.verification.rate}")
  public void verifyTekKeys() {

    logger.info("Fetching al authorizationCodes....");

    final LocalDateTime now = LocalDateTime.now();

    Iterable<AuthorizationCode> authorizationCodes = authorizationCodeRepository.findAll();

    logger.info("Fetched {} acs", StreamSupport.stream(authorizationCodes.spliterator(), false).count());

    Map<String,AuthorizationCode> verifiedAcs = new HashMap<>();

    authorizationCodes.iterator().forEachRemaining(authorizationCode -> {

      List<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository
          .findByMobileTestIdOrMobileTestId2AndDatePatientInfectiousAndVerified(
              authorizationCode.getMobileTestId(),
              authorizationCode.getDatePatientInfectious(),
              false);

      logger.debug("Fetched keys for AC mobileTestId id {} = {}",
            authorizationCode.getMobileTestId(), diagnosisKeys.size());

      diagnosisKeys.iterator().forEachRemaining(diagnosisKey -> {
        try {
          boolean verified1 = this.cryptoUtils.verify(
              diagnosisKey.getSignatureData(),
              authorizationCode.getSignature());

          boolean verified2 = this.cryptoUtils.verify(
              diagnosisKey.getSignatureData2(),
              authorizationCode.getSignature());

          boolean verified = verified1 || verified2;

          if (!verified) {
            logger.warn("DiagnosisKey for mobileTestId {} verification result = {}",
                diagnosisKey.getMobileTestId(), verified);
          }

          if (verified) {
            verifiedAcs.put(authorizationCode.getSignature(),authorizationCode);
            submissionMonitor.incrementRealRequestCounter();
            verifyDiagnosisKey(diagnosisKey);
          }

        } catch (Exception e) {
          logger.error("Unable to verify TEK {} due to {}", diagnosisKey.getMobileTestId(), e.getMessage(), e);
        }
      });


    });

    verifiedAcs.keySet().forEach(ac -> submissionMonitor.incrementAcVerified());

    logger.info("Removing {} verified ACs", verifiedAcs.size());
    removeVerifiedAcs(verifiedAcs);

    //verifiedAcs.forEach(ac -> authorizationCodeRepository.delete(ac));

    LocalDateTime end = LocalDateTime.now();

    logger.info("Duration = {}", ChronoUnit.SECONDS.between(now, end));
  }


  /**
   * Mark the diagnosiskey as verified and persist it.
   *
   * @param diagnosisKey The diagnosiskey to verify
   */
  @Transactional
  public void verifyDiagnosisKey(DiagnosisKey diagnosisKey) {
    diagnosisKey.setVerified(true);
    diagnosisKeyRepository.save(diagnosisKey);
  }

  /**
   * Remove the verified ACs.
   *
   * @param verifiedAcs the list of verified ACs we need to remove.
   */
  @Transactional
  public void removeVerifiedAcs(Map<String, AuthorizationCode> verifiedAcs) {
    AtomicInteger count = new AtomicInteger(0);
    verifiedAcs.values().forEach(ac -> {
      logger.info("Removing AC for {}", ac.getMobileTestId());
      count.getAndIncrement();
      authorizationCodeRepository.deleteAuthorizationCodeForMobileTestId(ac.getMobileTestId());
    });

    logger.info("Removed {} ACs",count.get());
  }

}
