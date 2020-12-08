

package app.coronawarn.server.common.persistence.domain;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.Size;

/**
 * This entity is mapped to a table which serves as data source for the
 * uploading of diagnosis keys to the Federation Gateway Service. The underlying table
 * is filled by replicating diagnosis keys on insert (currently via database triggers).
 *
 * <p>The entity is almost equivalent to the {@link DiagnosisKey} in content, but carries
 * some additional specific metadata, and could diverge in the future.
 */
public class FederationUploadKey extends DiagnosisKey {

  private String batchTag;

  FederationUploadKey(byte[] keyData, int rollingStartIntervalNumber, int rollingPeriod, int transmissionRiskLevel,
      long submissionTimestamp,
      String mobileTestId, String mobileTestId2,
      LocalDate datePatientInfectious, LocalDate dateTestCommunicated, int resultChannel,
      boolean consentToFederation, @Size String originCountry, Set<String> visitedCountries,
      ReportType reportType, int daysSinceOnsetOfSymptoms,boolean verified) {
    super(keyData, rollingStartIntervalNumber, rollingPeriod, transmissionRiskLevel, submissionTimestamp,
        mobileTestId, mobileTestId2,
        datePatientInfectious, dateTestCommunicated, resultChannel,
        consentToFederation, originCountry, visitedCountries, reportType, daysSinceOnsetOfSymptoms,verified);
  }

  public String getBatchTag() {
    return batchTag;
  }

  /**
   * Create a new instance of an upload key by copying the properties of the given source diagnosis key.
   */
  public static FederationUploadKey from(DiagnosisKey diagnosisKeySource) {
    return new FederationUploadKey(diagnosisKeySource.getKeyData(), diagnosisKeySource.getRollingStartIntervalNumber(),
        diagnosisKeySource.getRollingPeriod(), diagnosisKeySource.getTransmissionRiskLevel(),
        diagnosisKeySource.getSubmissionTimestamp(),

        diagnosisKeySource.getMobileTestId(),diagnosisKeySource.getMobileTestId2(),
        diagnosisKeySource.getDatePatientInfectious(),diagnosisKeySource.getDateTestCommunicated(),
        diagnosisKeySource.getResultChannel(),

        diagnosisKeySource.isConsentToFederation(),
        diagnosisKeySource.getOriginCountry(), diagnosisKeySource.getVisitedCountries(),
        diagnosisKeySource.getReportType(), diagnosisKeySource.getDaysSinceOnsetOfSymptoms(),
        diagnosisKeySource.isVerified());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FederationUploadKey that = (FederationUploadKey) o;
    return super.equals(o) && Objects.equals(batchTag, that.batchTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), batchTag);
  }
}
