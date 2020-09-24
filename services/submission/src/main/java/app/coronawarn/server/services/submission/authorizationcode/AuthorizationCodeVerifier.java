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
import app.coronawarn.server.services.submission.util.CryptoUtils;
import java.util.List;
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
  @Scheduled(fixedDelayString = "${services.submission.verification.rate}")
  @Transactional
  public void verifyTekKeys() {

    logger.info("Fetching al authorizationCodes....");
    Iterable<AuthorizationCode> authorizationCodes = authorizationCodeRepository.findAll();

    authorizationCodes.iterator().forEachRemaining(authorizationCode -> {

      List<DiagnosisKey> diagnosisKeys = diagnosisKeyRepository
          .findByMobileTestIdAndDatePatientInfectious(authorizationCode.getMobileTestId(),
              authorizationCode.getDatePatientInfectious());

      logger.debug("Fetched DiagnosisKeys {} for AC {}", diagnosisKeys,authorizationCode.getMobileTestId());

      diagnosisKeys.iterator().forEachRemaining(diagnosisKey -> {
        try {
          boolean verified = this.cryptoUtils.verify(
              diagnosisKey.getSignatureData(),
              authorizationCode.getSignature());
          logger.debug("DiagnosisKey for mobileTestId {} verification result = {}",
              diagnosisKey.getMobileTestId(), verified);
          diagnosisKey.setVerified(verified);
          diagnosisKeyRepository.save(diagnosisKey);
        } catch (Exception e) {
          logger.error("Unable to verify TEK {} due to {}", diagnosisKey.getMobileTestId(), e.getMessage(), e);
        }
      });
    });

  }

}
