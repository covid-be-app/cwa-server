package app.coronawarn.server.services.distribution.persistence.domain;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "export_batch")
public class ExportBatch {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Instant fromTimestamp;

  private Instant toTimestamp;

  private ExportBatchStatus status;

  private ExportConfiguration configuration;

  protected ExportBatch() {
  }

  ExportBatch(Instant fromTimestamp, Instant toTimestamp, ExportBatchStatus status, ExportConfiguration configuration) {
    this.fromTimestamp = fromTimestamp;
    this.toTimestamp = toTimestamp;
    this.status = status;
    this.configuration = configuration;
  }

  /**
   * Returns the export batch id.
   */
  public Long getId() {
    return id;
  }

  /**
   * Returns the timestamp from which on diagnosis keys are included.
   */
  public Instant getFromTimestamp() {
    return fromTimestamp;
  }

  /**
   * Returns the timestamp to which diagnosis keys are included.
   */
  public Instant getToTimestamp() {
    return toTimestamp;
  }

  /**
   * Returns the status of the export batch.
   */
  public ExportBatchStatus getStatus() {
    return status;
  }

  /**
   * Returns the configuration that has been used to generate this export batch.
   */
  public ExportConfiguration getConfiguration() {
    return configuration;
  }
}