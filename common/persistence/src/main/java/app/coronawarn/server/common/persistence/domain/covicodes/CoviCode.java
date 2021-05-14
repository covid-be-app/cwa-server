package app.coronawarn.server.common.persistence.domain.covicodes;

import app.coronawarn.server.common.persistence.domain.covicodes.CoviCodesBuilders.FinalBuilder;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

public class CoviCode implements Persistable<String> {

  @Id
  private String code;

  private LocalDateTime startInterval;

  private LocalDateTime endInterval;

  private CoviCodeStatus status;

  /**
   * No args constructor.
   */
  public CoviCode() {
  }

  /**
   * Builds a CoviCode.
   *
   * @param code  The actual code
   * @param startInterval The start interval (UTC)
   * @param endInterval The end interval (UTC)
   * @param status The status
   *
   */
  public CoviCode(String code, LocalDateTime startInterval, LocalDateTime endInterval, CoviCodeStatus status) {
    this.code = code;
    this.startInterval = startInterval;
    this.endInterval = endInterval;
    this.status = status;
  }

  public static FinalBuilder builder() {
    return new CoviCodeBuilder();
  }


  public String getCode() {
    return code;
  }

  public LocalDateTime getStartInterval() {
    return startInterval;
  }

  public LocalDateTime getEndInterval() {
    return endInterval;
  }

  public CoviCodeStatus getStatus() {
    return status;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setStartInterval(LocalDateTime startInterval) {
    this.startInterval = startInterval;
  }

  public void setEndInterval(LocalDateTime endInterval) {
    this.endInterval = endInterval;
  }

  public void setStatus(CoviCodeStatus status) {
    this.status = status;
  }

  /**
   * CoviCodes have a validity of 24 minutes.
   * <p>
   * The start and end time for that validity is based on the interval the code has been issued.
   * CoviCodes are issues based on intervals. (ex: during a 5 minute period (ex: between 10:00 to 10:05)
   * </p>
   * <p>
   * For example, if a code is issued between 16:50:00 and 16:54:59
   * it is valid between 16:48 and 17:12
   * </p>
   * <p>
   * if a code is issued between 00:00 and 00:04
   * it is valid between 23:58 (previous day) and 00:22.
   * </p>
   * <p>
   * So when we need to check a code at a certain time, we first need to determine the current interval we are in.
   * For example, if a code is verified at
   * </p>
   *
   * @return
   */
  public boolean isValid(Clock clock) {
    LocalDateTime now = LocalDateTime.now(clock);

    LocalDateTime startValidity = startInterval.minusMinutes(2);
    LocalDateTime endValidity = startValidity.plusMinutes(2 + 20 + 2);

    return now.isAfter(startValidity) && now.isBefore(endValidity);
  }

  public boolean isValid() {
    return isValid(Clock.systemUTC());
  }



  @Override
  public String getId() {
    return code;
  }

  @Override
  public boolean isNew() {
    return true;
  }

  @Override
  public String toString() {
    return "CoviCode{"
        + "code='" + code + '\''
        + ", startInterval=" + startInterval
        + ", endInterval=" + endInterval
        + ", status=" + status
        + '}';
  }

}
