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

import static app.coronawarn.server.common.persistence.utils.CryptoUtils.decodeAesKey;
import static java.time.LocalDate.parse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class R1CalculatorTest {

  private static final String EMPTY_SUFFIX = "";

  /*
     * K = +VhBgVyOB96AX1NHqEyibA==
     * R0 = "uyVJlD1sfiSZkHDR"
     * t0 = "2020-07-21"
     * info = uyVJlD1sfiSZkHDR2020-07-21TEST REQUEST
     * n1 978276
     * n2 556884
     * n3 394
     * (R1: 978276556884394, R1WithCheck: 97827655688439470)
     */
    @Test
    public void test() throws Exception {
        R1Calculator r1Calculator = new R1Calculator(parse("2020-07-21"),"uyVJlD1sfiSZkHDR", decodeAesKey("+VhBgVyOB96AX1NHqEyibA=="));
        String R1 = r1Calculator.generate15Digits();
        Assertions.assertThat(R1).isEqualTo("978276556884394");
    }

    /*
     * K = j9EWWBZYt9CWsGtTpPNUrg==
     * R0 = tlA1nDLx0PE0QlVN
     * t0 = "2020-07-21"
     * info = tlA1nDLx0PE0QlVN2020-07-21TEST REQUEST
     * n1 989250
     * n2 150432
     * n3 575
     * (R1: 989250150432575, R1WithCheck: 98925015043257584)
     */
    @Test
    public void test2() throws Exception {
        R1Calculator r1Calculator = new R1Calculator(parse("2020-07-21"),"tlA1nDLx0PE0QlVN", decodeAesKey("j9EWWBZYt9CWsGtTpPNUrg=="));
        String R1 = r1Calculator.generate15Digits();
        Assertions.assertThat(R1).isEqualTo("989250150432575");
    }

    /*
     * K = bJxx/UPRKwPwdadwJTs76w==
     * R0 = RNJ7XO0sP88xextu
     * t0 = "2020-07-21"
     * info = RNJ7XO0sP88xextu2020-07-21TEST REQUEST
     * n1 402570
     * n2 780892
     * n3 356
     * (R1: 402570780892356, R1WithCheck: 40257078089235696)
     */
    @Test
    public void test3() throws Exception {
        R1Calculator r1Calculator = new R1Calculator(parse("2020-07-21"),"RNJ7XO0sP88xextu", decodeAesKey("bJxx/UPRKwPwdadwJTs76w=="));
        String R1 = r1Calculator.generate15Digits();
        Assertions.assertThat(R1).isEqualTo("402570780892356");
    }

    /*
     * K = 8FQZ4I4BT66ClgTmnM1Alw==
     * R0 = 2nii5Uwaga2GAsiJ
     * t0 = "2020-07-21"
     * info = 2nii5Uwaga2GAsiJ2020-07-21TEST REQUEST
     * n1 310169
     * n2 445554
     * n3 293
     * (R1: 310169445554293, R1WithCheck: 31016944555429322)
     */
    @Test
    public void test4() throws Exception {
        R1Calculator r1Calculator = new R1Calculator(parse("2020-07-21"),"2nii5Uwaga2GAsiJ", decodeAesKey("8FQZ4I4BT66ClgTmnM1Alw=="));
        String R1 = r1Calculator.generate15Digits();
        Assertions.assertThat(R1).isEqualTo("310169445554293");
    }

  /*
   * K = 8FQZ4I4BT66ClgTmnM1Alw==
   * R0 = 2nii5Uwaga2GAsiJ
   * t0 = "2020-08-27"
   * info = 2nii5Uwaga2GAsiJ2020-08-27TEST REQUEST
   * (R1: 310169445554293, R1WithCheck: 31016944555429322)
   */
  @Test
  public void test5() throws Exception {
    R1Calculator r1Calculator = new R1Calculator(parse("2020-08-27"),"2nii5Uwaga2GAsiJ", decodeAesKey("8FQZ4I4BT66ClgTmnM1Alw=="));
    String R1 = r1Calculator.generate15Digits();
    Assertions.assertThat(R1).isEqualTo("832102846717000");
  }

  /*
   * K = enaeEaH/7zxo8/4RUFtidQ==
   * R0 = ryinAKH0AoVXXLwM
   * t0 = "2020-09-08"
   * info = ryinAKH0AoVXXLwM2020-09-10TEST REQUEST
   * (R1: 310169445554293, R1WithCheck: 31016944555429322)
   *
   * l1 = 374838
   * l2 = 1033537
   * l3 = 389
   * l1 % 1000000 = 374838
   * l2 % 1000000 = 33537
   * l3 % 1000 = 389
   *
   */
  @Test
  public void test6() throws Exception {
    R1Calculator r1Calculator = new R1Calculator(parse("2020-09-08"),"ryinAKH0AoVXXLwM", decodeAesKey("enaeEaH/7zxo8/4RUFtidQ=="));
    String R1 = r1Calculator.generate15Digits();
    Assertions.assertThat(R1).isEqualTo("374838033537389");

  }

  @Test
  public void test7_ios() throws Exception {
    R1Calculator r1Calculator = new R1Calculator(parse("2020-09-29"),"kmplncnleflcmfoa", decodeAesKey("574htzp3ztPpHi2n1XZzXQ=="));
    String R1 = r1Calculator.generate15Digits();
    Assertions.assertThat(R1).isEqualTo("865547380926110");
  }

  /**
   * Here the scenario was that the Android phone was generating an R1 = 497226217372589
   * test results were submitted and polled using that R1
   * backend however, upon TEK submission would re-generate the R1 to be 865547380926110.
   * In that sceario, backend would see authorization codes for 497226217372589 but TEK submissions for 865547380926110
   * These would never get validated.
   *
   * @throws Exception
   */
  @Test
  public void test7_android() throws Exception {
    R1Calculator r1Calculator = new R1Calculator(parse("2020-09-29"),"kmplncnleflcmfoa", EMPTY_SUFFIX, decodeAesKey("574htzp3ztPpHi2n1XZzXQ=="));
    String R1 = r1Calculator.generate15Digits();
    Assertions.assertThat(R1).isEqualTo("497226217372589");
  }

  @Test
  public void test8_ios() throws Exception {
    R1Calculator r1Calculator = new R1Calculator(parse("2020-09-29"),"hnjnkhhoopiicbin", decodeAesKey("2RIc5F9TbCQgwcBpE/20wg=="));
    String R1 = r1Calculator.generate15Digits();
    Assertions.assertThat(R1).isEqualTo("160483255896159");
  }

  @Test
  public void test8_android() throws Exception {
    R1Calculator r1Calculator = new R1Calculator(parse("2020-09-29"),"hnjnkhhoopiicbin", EMPTY_SUFFIX,decodeAesKey("2RIc5F9TbCQgwcBpE/20wg=="));
    String R1 = r1Calculator.generate15Digits();
    Assertions.assertThat(R1).isEqualTo("581424823445608");
  }

}
