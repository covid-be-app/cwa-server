package app.coronawarn.server.services.submission;

import static app.coronawarn.server.services.submission.util.CryptoUtils.TEXT;
import static app.coronawarn.server.services.submission.util.CryptoUtils.generateHash;
import static java.lang.Byte.toUnsignedLong;
import static java.util.Arrays.copyOfRange;

import java.math.BigInteger;
import java.time.LocalDate;
import javax.crypto.SecretKey;

public class R1Calculator {

  private final LocalDate datePatientInfectiuous;
  private final String randomString;
  private final SecretKey secretKey;

  /**
   * Constructs the R1 Calculator.
   */
  public R1Calculator(LocalDate datePatientInfectiuous, String randomString, SecretKey secretKey) {
    this.datePatientInfectiuous = datePatientInfectiuous;
    this.randomString = randomString;
    this.secretKey = secretKey;
  }

  public SecretKey getSecretKey() {
    return secretKey;
  }

  public LocalDate getDatePatientInfectiuous() {
    return datePatientInfectiuous;
  }

  public String getRandomString() {
    return randomString;
  }

  /**
   * Main function to generate 15 digits based on our R0 , t0 and K.
   */
  public String generate15Digits() throws Exception {
    byte[] hash = generateHash(randomString.toString() + datePatientInfectiuous + TEXT, secretKey);
    byte[] reducedHash = copyOfRange(hash, hash.length - 7, hash.length);

    long l1 = (toUnsignedLong(reducedHash[0]))
        + (toUnsignedLong(reducedHash[1]) << 8)
        + (((toUnsignedLong(reducedHash[2]) & 0xF) << 16));

    long l2 = (toUnsignedLong(reducedHash[2]) >> 4)
        + (toUnsignedLong(reducedHash[3]) << 4)
        + (toUnsignedLong(reducedHash[4]) << 12);

    long l3 = (toUnsignedLong(reducedHash[5]))
        + (((toUnsignedLong(reducedHash[6])) & 0x3) << 8);

    BigInteger bigInteger = new BigInteger(String.format("%d%d%d", l1 % 1000000, l2 % 1000000, l3 % 1000));
    return String.format("%15d", bigInteger).replace(' ', '0');

  }

}
