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

package app.coronawarn.server.services.submission.covicode;

import app.coronawarn.server.common.persistence.repository.CoviCodeRepository;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This component will delete all authorization codes that are beyond a certain date.
 */
@Component
@Profile("covicode-cleanup")
public class CoviCodeCleanup {

  private static final Logger logger = LoggerFactory.getLogger(CoviCodeCleanup.class);

  private final CoviCodeRepository coviCodeRepository;
  private final SubmissionServiceConfig submissionServiceConfig;

  /**
   * Creates the authorization code cleanup component.
   */
  public CoviCodeCleanup(CoviCodeRepository coviCodeRepository,
                         SubmissionServiceConfig submissionServiceConfig) {
    this.coviCodeRepository = coviCodeRepository;
    this.submissionServiceConfig = submissionServiceConfig;
  }

  /**
   * Fetch all ACs that beyond a certain data and delete them.
   */
  @Scheduled(fixedDelayString = "${services.submission.cleanup.ac.rate}")
  @Transactional
  public void deleteOldCoviCodes() {
    LocalDate beforeDate = LocalDate.now().minusDays(submissionServiceConfig.getCleanup().getCoviCode().getDays());
    Integer deleted = coviCodeRepository.deleteObsoleteCoviCodes(beforeDate);
    logger.info("Cleanup old covicodes before {} : deleted {} covicodes.",beforeDate, deleted);
  }

}
