/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
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

package app.coronawarn.server.services.distribution.persistence.domain;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

  @ManyToOne(targetEntity = ExportConfiguration.class, fetch = FetchType.EAGER)
  @JoinColumn(name = "configuration_id")
  private ExportConfiguration configuration;

  protected ExportBatch() {
  }

  /**
   * Creates a new export batch.
   *
   * @param fromTimestamp  Timestamp as of which diagnosis keys should be considered.
   * @param toTimestamp Timestamp up to which diagnosis keys should be considered.
   * @param status The {@link ExportBatchStatus} of the current batch.
   * @param configuration The {@link ExportConfiguration}, which has been used to create this batch.
   */
  public ExportBatch(Instant fromTimestamp, Instant toTimestamp, ExportBatchStatus status,
                     ExportConfiguration configuration) {
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