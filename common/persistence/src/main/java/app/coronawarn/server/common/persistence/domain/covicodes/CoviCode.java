package app.coronawarn.server.common.persistence.domain.covicodes;

import java.time.LocalDateTime;

public class CoviCode {

  private String code;
  private LocalDateTime startInterval;
  private LocalDateTime endInterval;
  private CoviCodeStatus status;



}
