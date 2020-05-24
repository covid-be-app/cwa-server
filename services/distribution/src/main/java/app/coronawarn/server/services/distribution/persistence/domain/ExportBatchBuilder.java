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

import static app.coronawarn.server.services.distribution.persistence.domain.ExportBatchBuilders.Builder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportBatchBuilders.ExportConfigurationBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportBatchBuilders.FinalBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportBatchBuilders.StatusBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportBatchBuilders.ToTimeStampBuilder;

import app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilder;
import app.coronawarn.server.services.distribution.persistence.exception.InvalidExportBatchException;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportBatchBuilder implements Builder, ToTimeStampBuilder, StatusBuilder, ExportConfigurationBuilder,
        FinalBuilder  {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBuilder.class);

  private Instant fromTimestamp;
  private Instant toTimestamp;
  private ExportBatchStatus status;
  private ExportConfiguration configuration;

  @Override
  public ToTimeStampBuilder withFromTimestamp(Instant fromTimestamp) {
    this.fromTimestamp = fromTimestamp;
    return this;
  }

  @Override
  public StatusBuilder withToTimestamp(Instant toTimestamp) {
    this.toTimestamp = toTimestamp;
    return this;
  }

  @Override
  public ExportConfigurationBuilder withStatus(ExportBatchStatus status) {
    this.status = status;
    return this;
  }

  @Override
  public FinalBuilder withExportConfiguration(ExportConfiguration configuration) {
    this.configuration = configuration;
    return this;
  }

  @Override
  public ExportBatch build() {
    ExportBatch exportBatch = new ExportBatch(this.fromTimestamp, this.toTimestamp, this.status, this.configuration);

    return throwValidationFails(exportBatch);
  }

  private ExportBatch throwValidationFails(ExportBatch exportBatch) {
    Set<ConstraintViolation<ExportBatch>> violations = exportBatch.getConstraintViolations();

    if (!violations.isEmpty()) {
      String violationsMessage = violations.stream()
            .map(violation -> String.format("%s Invalid Value: %s", violation.getMessage(),
                    violation.getInvalidValue()))
            .collect(Collectors.toList()).toString();
      logger.debug(violationsMessage);
      throw new InvalidExportBatchException(violationsMessage);
    }

    return exportBatch;
  }
}
