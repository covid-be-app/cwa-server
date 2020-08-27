package app.coronawarn.server.services.submission;

import static app.coronawarn.server.services.submission.util.CryptoUtils.decodeAesKey;
import static java.time.LocalDate.parse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class R1CalculatorTest {

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
    Assertions.assertThat(R1).isEqualTo("008321028467170");
  }
}
