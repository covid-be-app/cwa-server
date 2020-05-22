package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.persistence.domain.ExportBatch;
import java.util.Set;

public class Export {

  private Set<DiagnosisKey> keys;

  private ExportBatch batch;

  public Export(Set<DiagnosisKey> keys, ExportBatch batch) {
    this.keys = keys;
    this.batch = batch;
  }

  public void setKeys(Set<DiagnosisKey> keys) {
    this.keys = keys;
  }

  public ExportBatch getBatch() {
    return batch;
  }

  public void setBatch(ExportBatch batch) {
    this.batch = batch;
  }

  public Set<DiagnosisKey> getKeys() {
    return keys;
  }

}
