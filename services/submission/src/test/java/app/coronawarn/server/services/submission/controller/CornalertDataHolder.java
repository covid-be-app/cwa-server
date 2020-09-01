package app.coronawarn.server.services.submission.controller;

import java.time.LocalDate;
import org.springframework.http.HttpHeaders;

public class CornalertDataHolder {

  private String secretKey;
  private String randomString;
  private LocalDate datePatientInfectious;
  private LocalDate dateTestCommunicated;
  private String mobileTestId;
  private String signature;
  private String resultChannel;

  public CornalertDataHolder(String secretKey, String randomString, LocalDate datePatientInfectious,
      LocalDate dateTestCommunicated,String mobileTestId, String signature, String resultChannel) {
    this.secretKey = secretKey;
    this.randomString = randomString;
    this.datePatientInfectious = datePatientInfectious;
    this.dateTestCommunicated = dateTestCommunicated;
    this.mobileTestId = mobileTestId;
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

  public String getMobileTestId() {
    return mobileTestId;
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
        .resultChannel(getResultChannel())
        .build();
  }

  public static CornalertDataHolder VALID_COMBO = new CornalertDataHolder(
      "8FQZ4I4BT66ClgTmnM1Alw==",
      "3nii5Uwaga2GAsiJ",
      LocalDate.parse("2020-08-27"),
      LocalDate.parse("2020-09-01"),
      "945647857314342",
      "3046022100ca62a9404a869dab0e301196ce82b14f31286c9a934cdbb9028efc62da7f5e" +
          "3f022100fe4d1a552997b177d9d9394114dc8fa3a066081c98acb15df0914b2973206feb",
      "1"
  );

  public static CornalertDataHolder INVALID_SIGNATURE = new CornalertDataHolder(
      "8FQZ4I4BT66ClgTmnM1Alw==",
      "3nii5Uwaga2GAsiJ",
      LocalDate.parse("2020-08-27"),
      LocalDate.parse("2020-09-01"),
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
      "945647857314342",
      "0000022100ca62a9404a869dab0e301196ce82b14f31286c9a934cdbb9028efc62da7f5e" +
          "3f022100fe4d1a552997b177d9d9394114dc8fa3a066081c98acb15df0914b2973206feb",
      "1"
  );
}
