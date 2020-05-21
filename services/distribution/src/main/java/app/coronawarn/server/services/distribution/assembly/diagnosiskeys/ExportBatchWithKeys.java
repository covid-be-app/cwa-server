package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.persistence.domain.ExportBatch;
import java.util.Set;

public class ExportBatchWithKeys extends ExportBatch{

  private Set<DiagnosisKey> keys;


  private String country;

  public String getCountry() {
    return country;
  }

  public Set<DiagnosisKey> getKeys() {
    return keys;
  }

}
