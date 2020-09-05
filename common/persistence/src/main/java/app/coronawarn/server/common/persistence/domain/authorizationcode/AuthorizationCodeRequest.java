package app.coronawarn.server.common.persistence.domain.authorizationcode;

import java.util.List;

public class AuthorizationCodeRequest {

  private List<AuthorizationCode> authorizationCodeEntities;

  public List<AuthorizationCode> getAuthorizationCodeEntities() {
    return authorizationCodeEntities;
  }

  public void setAuthorizationCodeEntities(List<AuthorizationCode> authorizationCodeEntities) {
    this.authorizationCodeEntities = authorizationCodeEntities;
  }
}
