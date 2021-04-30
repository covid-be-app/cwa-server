package app.coronawarn.server.common.persistence.domain.covicodes;

import static app.coronawarn.server.common.persistence.utils.CryptoUtils.decodeAesKey;
import static app.coronawarn.server.common.persistence.utils.CryptoUtils.generateHash;
import static java.util.Arrays.copyOfRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.BitSet;

public class CoviCodeGenerator {

  private static final int CODES_PER_INTERVAL = 200;
  private static final int MINUTES_IN_INTERVAL = 5;
  private static final int NR_OF_INTERVALS = (24 * 60) / MINUTES_IN_INTERVAL;

  /**
   * Generate the CoviCodes.
   */
  public void generate() throws Exception {
    LocalDateTime initial = LocalDateTime.of(2021, 4, 8, 0, 0, 0);

    StringBuffer output = new StringBuffer();
    for (int i = 0; i < NR_OF_INTERVALS; i++) {

      int startMinuteOffset = i * 5;
      LocalDateTime start = initial.plusMinutes(startMinuteOffset);
      LocalDateTime end = start.plusMinutes(5);

      for (int c = 0; c < CODES_PER_INTERVAL; c++) {
        String code = generate15Digits(initial.toLocalDate(), i, c);
        output
            .append(code + "," + start.toEpochSecond(ZoneOffset.UTC) + "," + end.toEpochSecond(ZoneOffset.UTC) + "\n");
      }

    }

    //FileUtils writeByteArrayToFile(new File("2021-04-08-codes.csv"),output.toString().getBytes());

  }


  /**
   *
   * Generate the 12 digit CoviCode.
   *
   * @param localDate
   * @param period
   * @param counter
   * @return
   * @throws Exception
   */
  private String generate15Digits(LocalDate localDate, int period, int counter) throws Exception {
    byte[] hash = generateHash("" + localDate + period + counter, decodeAesKey("+VhBgVyOB96AX1NHqEyibA=="));
    byte[] reducedHash = copyOfRange(hash, hash.length - 7, hash.length);

    BitSet bs = BitSet.valueOf(reducedHash);
    BitSet first20 = bs.get(0, 20);
    BitSet scnd20 = bs.get(20, 40);

    long l1 = first20.toLongArray()[0];
    long l2 = scnd20.toLongArray()[0];

    return String.format("%06d%06d", l1 % 1000000, l2 % 1000000);

  }


}
