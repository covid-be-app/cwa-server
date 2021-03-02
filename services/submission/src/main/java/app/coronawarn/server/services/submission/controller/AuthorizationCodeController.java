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

package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.authorizationcode.AuthorizationCode;
import app.coronawarn.server.common.persistence.domain.authorizationcode.AuthorizationCodeRequest;
import app.coronawarn.server.common.persistence.service.AuthorizationCodeService;
import app.coronawarn.server.services.submission.monitoring.SubmissionMonitor;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
@Profile("allow-authorizationcode-processing")
public class AuthorizationCodeController {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationCodeController.class);

  public static final String AC_PROCESS_PATH = "/authorizationcodes/process";

  private AuthorizationCodeService authorizationCodeService;
  private SubmissionMonitor submissionMonitor;

  AuthorizationCodeController(AuthorizationCodeService authorizationCodeService,SubmissionMonitor submissionMonitor) {
    this.authorizationCodeService = authorizationCodeService;
    this.submissionMonitor = submissionMonitor;
  }

  /**
   * Handles diagnosis key submission requests.
   *
   * @param authorizationCodeRequest The authorization codes that need to be saved.
   * @return An empty response body.
   */
  @PostMapping(value = AC_PROCESS_PATH)
  public ResponseEntity<Void> processAuthorizationCodes(
      @RequestBody AuthorizationCodeRequest authorizationCodeRequest) {
    List<AuthorizationCode> authorizationCodeEntities = authorizationCodeRequest.getAuthorizationCodeEntities();
    logger.debug("AC Transfer - Received {}",authorizationCodeEntities.size());
    authorizationCodeEntities.stream().forEach(ac -> {
      logger.debug("AC Transfer - Processing {}",ac);
      submissionMonitor.incrementAcs();
    });
    authorizationCodeService.saveAuthorizationCodes(authorizationCodeEntities);
    return ResponseEntity.noContent().build();
  }
}
