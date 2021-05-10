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

package app.coronawarn.server.services.submission;

import static app.coronawarn.server.common.persistence.utils.CryptoUtils.TEXT;
import static app.coronawarn.server.common.persistence.utils.CryptoUtils.generateHash;
import static java.lang.Byte.toUnsignedLong;
import static java.util.Arrays.copyOfRange;

import java.time.LocalDate;
import javax.crypto.SecretKey;

public class R1Calculator {

  private final LocalDate datePatientInfectious;
  private final String randomString;
  private final String textSuffix;
  private final SecretKey secretKey;

  /**
   * Constructs the R1 Calculator.
   */
  public R1Calculator(LocalDate datePatientInfectious, String randomString, String textSuffix, SecretKey secretKey) {
    this.datePatientInfectious = datePatientInfectious;
    this.randomString = randomString;
    this.textSuffix = textSuffix;
    this.secretKey = secretKey;
  }

  /**
   * Constructs the R1 Calculator.
   */
  public R1Calculator(LocalDate datePatientInfectious, String randomString, SecretKey secretKey) {
    this(datePatientInfectious,randomString,TEXT,secretKey);
  }

  /**
   * Main function to generate 15 digits based on our R0 , t0 and K.
   */
  public String generate15Digits() throws Exception {
    byte[] hash = generateHash(randomString.toString() + datePatientInfectious + textSuffix, secretKey);
    byte[] reducedHash = copyOfRange(hash, hash.length - 7, hash.length);

    long l1 = (toUnsignedLong(reducedHash[0]))
        + (toUnsignedLong(reducedHash[1]) << 8)
        + (((toUnsignedLong(reducedHash[2]) & 0xF) << 16));

    long l2 = (toUnsignedLong(reducedHash[2]) >> 4)
        + (toUnsignedLong(reducedHash[3]) << 4)
        + (toUnsignedLong(reducedHash[4]) << 12);

    long l3 = (toUnsignedLong(reducedHash[5]))
        + (((toUnsignedLong(reducedHash[6])) & 0x3) << 8);

    return String.format("%06d%06d%03d", l1 % 1000000, l2 % 1000000, l3 % 1000);


  }

}
