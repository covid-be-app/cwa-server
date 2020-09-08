package app.coronawarn.server.services.submission.authorizationcode;

import app.coronawarn.server.common.persistence.repository.AuthorizationCodeRepository;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This component will delete all authorization codes that are beyond a certain date.
 */
@Component
public class AuthorizationCodeCleanup {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationCodeCleanup.class);

  private final AuthorizationCodeRepository authorizationCodeRepository;
  private final SubmissionServiceConfig submissionServiceConfig;

  /**
   * Creates the authorization code cleanup component.
   */
  public AuthorizationCodeCleanup(AuthorizationCodeRepository authorizationCodeRepository,
      SubmissionServiceConfig submissionServiceConfig) {
    this.authorizationCodeRepository = authorizationCodeRepository;
    this.submissionServiceConfig = submissionServiceConfig;
  }

  /**
   * Fetch all ACs that beyond a certain data and delete them.
   */
  @Scheduled(fixedDelayString = "${services.submission.cleanup.ac.rate}")
  @Transactional
  public void deleteOldAuthorizationCodes() {
    LocalDate beforeDate = LocalDate.now().minusDays(submissionServiceConfig.getCleanup().getAc().getDays());
    Integer deleted = authorizationCodeRepository.deleteObsoleteAuthorizationCodes(beforeDate);
    logger.info("Cleanup old authorization codes before {} : deleted {} acs.",beforeDate, deleted);
  }

}
