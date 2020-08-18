package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.authorizationcode.AuthorizationCodeRequest;
import app.coronawarn.server.common.persistence.service.AuthorizationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
public class AuthorizationCodeController {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationCodeController.class);

  public static final String AC_PROCESS_PATH = "/authorizationcodes/process";

  private AuthorizationCodeService authorizationCodeService;

  AuthorizationCodeController(AuthorizationCodeService authorizationCodeService) {
    this.authorizationCodeService = authorizationCodeService;
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
    logger.info("Found {}",authorizationCodeRequest.getAuthorizationCodeEntities().size());
    authorizationCodeService.saveAuthorizationCodes(authorizationCodeRequest.getAuthorizationCodeEntities());
    return ResponseEntity.noContent().build();
  }
}
