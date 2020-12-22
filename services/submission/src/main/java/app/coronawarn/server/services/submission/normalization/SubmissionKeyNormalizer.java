

package app.coronawarn.server.services.submission.normalization;

import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.domain.normalization.DiagnosisKeyNormalizer;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.time.LocalDate;
import java.util.Objects;

public final class SubmissionKeyNormalizer implements DiagnosisKeyNormalizer {

  private TekFieldDerivations tekFieldMappings;
  private LocalDate dateOnsetOfSymptoms;

  public SubmissionKeyNormalizer(SubmissionServiceConfig config, LocalDate dateOnsetOfSymptoms) {
    tekFieldMappings = config.getTekFieldDerivations();
    this.dateOnsetOfSymptoms = dateOnsetOfSymptoms;
  }

  @Override
  public NormalizableFields normalize(NormalizableFields fieldsAndValues) {
    Integer trlValue = fieldsAndValues.getTransmissionRiskLevel();
    Integer dsosValue = fieldsAndValues.getDaysSinceOnsetOfSymptoms();

    throwIfAllRequiredFieldsMissing(trlValue, dsosValue);

    if (isMissing(dsosValue)) {
      dsosValue = tekFieldMappings.deriveDaysSinceSymptomsFromTransmissionRiskLevel(trlValue);
    } else if (isMissing(trlValue)) {
      trlValue = tekFieldMappings.deriveTransmissionRiskLevelFromDaysSinceSymptoms(dsosValue);
    }

    return NormalizableFields.of(trlValue, dsosValue);
  }

  private void throwIfAllRequiredFieldsMissing(Integer trlValue, Integer dsos) {
    if (isMissing(trlValue) && isMissing(dsos)) {
      throw new IllegalArgumentException("Normalization of key values failed. A key was provided with"
          + " both 'transmissionRiskLevel' and 'daysSinceOnsetOfSymptoms' fields missing");
    }
  }

  private boolean isMissing(Integer fieldValue) {
    return Objects.isNull(fieldValue);
  }
}
