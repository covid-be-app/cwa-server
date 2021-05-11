package app.coronawarn.server.common.persistence.domain.covicodes;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CoviCodePK implements Serializable {

  private String code;
  private LocalDateTime startInterval;
  private LocalDateTime endInterval;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public LocalDateTime getStartInterval() {
    return startInterval;
  }

  public void setStartInterval(LocalDateTime startInterval) {
    this.startInterval = startInterval;
  }

  public LocalDateTime getEndInterval() {
    return endInterval;
  }

  public void setEndInterval(LocalDateTime endInterval) {
    this.endInterval = endInterval;
  }
}
