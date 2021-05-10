package app.coronawarn.server.common.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.common.persistence.domain.covicodes.CoviCode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

public class CoviCodeTest {

  private static Clock getFixedClock(String datetime) {
    return Clock.fixed(
        Instant.parse(datetime),
        ZoneOffset.UTC);
  }

 @Test
  public void covidCodeNotValidBecauseTooEarly() {

    CoviCode coviCode = CoviCode.builder()
        .withCode("123456789012")
        .withStartInterval(LocalDateTime.parse("2021-05-01T10:00:00"))
        .withEndInterval(LocalDateTime.parse("2021-05-01T10:05:00"))
        .build();

    assertFalse(coviCode.isValid(getFixedClock("2021-05-01T09:57:00Z")));

  }

  @Test
  public void covidCodeValidBecauseBecauseEarlyWithinSkew() {

    CoviCode coviCode = CoviCode.builder()
        .withCode("123456789012")
        .withStartInterval(LocalDateTime.parse("2021-05-01T10:00:00"))
        .withEndInterval(LocalDateTime.parse("2021-05-01T10:05:00"))
        .build();

    assertTrue(coviCode.isValid(getFixedClock("2021-05-01T09:59:00Z")));

  }

  @Test
  public void covidCodeValidBecauseBecauseInInterval() {

    CoviCode coviCode = CoviCode.builder()
        .withCode("123456789012")
        .withStartInterval(LocalDateTime.parse("2021-05-01T10:00:00"))
        .withEndInterval(LocalDateTime.parse("2021-05-01T10:05:00"))
        .build();

    assertTrue(coviCode.isValid(getFixedClock("2021-05-01T10:02:00Z")));

  }

  @Test
  public void covidCodeNotValidBecauseTooLate() {

    CoviCode coviCode = CoviCode.builder()
        .withCode("123456789012")
        .withStartInterval(LocalDateTime.parse("2021-05-01T10:00:00"))
        .withEndInterval(LocalDateTime.parse("2021-05-01T10:05:00"))
        .build();

    assertFalse(coviCode.isValid(getFixedClock("2021-05-01T10:22:00Z")));

  }

  @Test
  public void covidCodeValidBecauseLateButInSkew() {

    CoviCode coviCode = CoviCode.builder()
        .withCode("123456789012")
        .withStartInterval(LocalDateTime.parse("2021-05-01T10:00:00"))
        .withEndInterval(LocalDateTime.parse("2021-05-01T10:05:00"))
        .build();

    assertTrue(coviCode.isValid(getFixedClock("2021-05-01T10:21:00Z")));
  }

  @Test
  public void covidCodeValidBecauseInInterval() {

    CoviCode coviCode = CoviCode.builder()
        .withCode("123456789012")
        .withStartInterval(LocalDateTime.parse("2021-05-01T10:00:00"))
        .withEndInterval(LocalDateTime.parse("2021-05-01T10:05:00"))
        .build();

    assertTrue(coviCode.isValid(getFixedClock("2021-05-01T10:03:00Z")));
  }

  @Test
  public void covidCodeValidBecauseInInterval2() {

    CoviCode coviCode = CoviCode.builder()
        .withCode("123456789012")
        .withStartInterval(LocalDateTime.parse("2021-05-01T10:00:00"))
        .withEndInterval(LocalDateTime.parse("2021-05-01T10:05:00"))
        .build();

    assertTrue(coviCode.isValid(getFixedClock("2021-05-01T10:15:00Z")));
  }


}
