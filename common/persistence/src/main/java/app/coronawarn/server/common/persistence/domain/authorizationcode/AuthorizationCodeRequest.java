package app.coronawarn.server.common.persistence.domain.authorizationcode;

import java.util.List;

public class AuthorizationCodeRequest {

  private List<AuthorizationCodeEntity> authorizationCodeEntities;

  public List<AuthorizationCodeEntity> getAuthorizationCodeEntities() {
    return authorizationCodeEntities;
  }

  public void setAuthorizationCodeEntities(List<AuthorizationCodeEntity> authorizationCodeEntities) {
    this.authorizationCodeEntities = authorizationCodeEntities;
  }
}
