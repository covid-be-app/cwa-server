package app.coronawarn.server.services.submission.controller;

import java.time.LocalDate;
import org.springframework.http.HttpHeaders;

public class CornalertDataHolder {

  private String secretKey;
  private String randomString;
  private LocalDate datePatientInfectious;
  private LocalDate dateTestCommunicated;
  private LocalDate dateOnsetOfSymptoms;
  private String mobileTestId;
  private String mobileTestId2;
  private String signature;
  private String resultChannel;

  public CornalertDataHolder(String secretKey, String randomString,
      LocalDate datePatientInfectious, LocalDate dateTestCommunicated,LocalDate dateOnsetOfSymptoms,
      String mobileTestId, String mobileTestId2, String signature,
      String resultChannel) {
    this.secretKey = secretKey;
    this.randomString = randomString;
    this.datePatientInfectious = datePatientInfectious;
    this.dateTestCommunicated = dateTestCommunicated;
    this.dateOnsetOfSymptoms = dateOnsetOfSymptoms;
    this.mobileTestId = mobileTestId;
    this.mobileTestId2 = mobileTestId2;
    this.signature = signature;
    this.resultChannel = resultChannel;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public String getRandomString() {
    return randomString;
  }

  public LocalDate getDatePatientInfectious() {
    return datePatientInfectious;
  }

  public LocalDate getDateTestCommunicated() {
    return dateTestCommunicated;
  }

  public LocalDate getDateOnsetOfSymptoms() {
    return dateOnsetOfSymptoms;
  }

  public String getMobileTestId() {
    return mobileTestId;
  }

  public String getMobileTestId2() {
    return mobileTestId2;
  }

  public String getSignature() {
    return signature;
  }

  public String getResultChannel() {
    return resultChannel;
  }

  public HttpHeaders buildHeader() {
    return HttpHeaderBuilder.builder()
        .contentTypeProtoBuf()
        .randomString(getRandomString())
        .secretKey(getSecretKey())
        .datePatientInfectious(getDatePatientInfectious())
        .dateTestCommunicated(getDateTestCommunicated())
        .dateOnsetOfSymptoms(getDateOnsetOfSymptoms())
        .resultChannel(getResultChannel())
        .build();
  }

  public static CornalertDataHolder VALID_COMBO = new CornalertDataHolder(
      "8FQZ4I4BT66ClgTmnM1Alw==",
      "3nii5Uwaga2GAsiJ",
      LocalDate.parse("2020-08-27"),
      LocalDate.parse("2020-09-01"),
      LocalDate.parse("2020-08-27"),
      "945647857314342",
      "945647857314342",
      "3046022100ca62a9404a869dab0e301196ce82b14f31286c9a934cdbb9028efc62da7f5e" +
          "3f022100fe4d1a552997b177d9d9394114dc8fa3a066081c98acb15df0914b2973206feb",
      "1"
  );

  public static CornalertDataHolder ANDROID_WRONG_R1_SIGNATURE_PAYLOAD = new CornalertDataHolder(
      "574htzp3ztPpHi2n1XZzXQ==",
      "kmplncnleflcmfoa",
      LocalDate.parse("2020-09-29"),
      LocalDate.parse("2020-10-02"),
      LocalDate.parse("2020-09-29"),
      "497226217372589",
      "497226217372589",  // The android incorrectly generated R1 stored in system and AC
      "3045022100a4fc68d585fdb75982778f7e1489c08bc1a72d6a74dfeeeb810dd0c2cee38f26022033e5df14261466393367aa20acc77a5876626f95f5f9a7e2ec943d0fc8bc58e0",
      "1"
  );

  public static CornalertDataHolder ANDROID_BACKEND_INTERPRETED_R1_PAYLOAD = new CornalertDataHolder(
      "574htzp3ztPpHi2n1XZzXQ==",
      "kmplncnleflcmfoa",
      LocalDate.parse("2020-09-29"),
      LocalDate.parse("2020-10-02"),
      LocalDate.parse("2020-09-29"),
      "865547380926110",   // The correct R1 generation based on incoming secret / random
      "497226217372589",  // The "android" incorrect R1 generation that will now also be "supported" by the backend
      "3045022100a4fc68d585fdb75982778f7e1489c08bc1a72d6a74dfeeeb810dd0c2cee38f26022033e5df14261466393367aa20acc77a5876626f95f5f9a7e2ec943d0fc8bc58e0",
      "1"
  );



  public static CornalertDataHolder INVALID_SIGNATURE = new CornalertDataHolder(
      "8FQZ4I4BT66ClgTmnM1Alw==",
      "3nii5Uwaga2GAsiJ",
      LocalDate.parse("2020-08-27"),
      LocalDate.parse("2020-09-01"),
      LocalDate.parse("2020-08-27"),
      "945647857314342",
      "945647857314342",
      "000000000000000000000000000000000000000000000000000000000000000000000000" +
          "000000000000000000000000000000000000000000000000000000000000000000000000",
      "1"
  );

  public static CornalertDataHolder INVALID_MOBILE_TEST_ID = new CornalertDataHolder(
      "8FQZ4I4BT66ClgTmnM1Alw==",
      "3nii5Uwaga2GAsiJ",
      LocalDate.parse("2020-08-27"),
      LocalDate.parse("2020-09-01"),
      LocalDate.parse("2020-08-27"),
      "123456789012345",
      "123456789012345",
      "3046022100ca62a9404a869dab0e301196ce82b14f31286c9a934cdbb9028efc62da7f5e" +
          "3f022100fe4d1a552997b177d9d9394114dc8fa3a066081c98acb15df0914b2973206feb",
      "1"
  );

  public static CornalertDataHolder INVALID_COMMUNICATION_DATE = new CornalertDataHolder(
      "8FQZ4I4BT66ClgTmnM1Alw==",
      "3nii5Uwaga2GAsiJ",
      LocalDate.parse("2020-08-27"),
      LocalDate.parse("2020-08-27"),
      LocalDate.parse("2020-08-27"),
      "945647857314342",
      "945647857314342",
      "3046022100ca62a9404a869dab0e301196ce82b14f31286c9a934cdbb9028efc62da7f5e" +
          "3f022100fe4d1a552997b177d9d9394114dc8fa3a066081c98acb15df0914b2973206feb",
      "1"
  );

  public static CornalertDataHolder INVALID_INFECTIOUS_DATE = new CornalertDataHolder(
      "8FQZ4I4BT66ClgTmnM1Alw==",
      "3nii5Uwaga2GAsiJ",
      LocalDate.parse("2020-09-01"),
      LocalDate.parse("2020-09-01"),
      LocalDate.parse("2020-09-01"),
      "945647857314342",
      "945647857314342",
      "0000022100ca62a9404a869dab0e301196ce82b14f31286c9a934cdbb9028efc62da7f5e" +
          "3f022100fe4d1a552997b177d9d9394114dc8fa3a066081c98acb15df0914b2973206feb",
      "1"
  );
}
