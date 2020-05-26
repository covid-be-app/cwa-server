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

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.Export;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.persistence.domain.ExportBatch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import app.coronawarn.server.services.distribution.persistence.domain.ExportBatchStatus;
import app.coronawarn.server.services.distribution.persistence.domain.ExportConfiguration;
import app.coronawarn.server.services.distribution.persistence.service.ExportBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Retrieves stored diagnosis keys and builds a {@link DiagnosisKeysDirectory} with them.
 */
@Component
public class DiagnosisKeysStructureProvider {

  private static final Logger logger = LoggerFactory
      .getLogger(DiagnosisKeysStructureProvider.class);

  private final DiagnosisKeyService diagnosisKeyService;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;
  private final ExportBatchService exportBatchService;


  /**
   * Creates a new DiagnosisKeysStructureProvider.
   */
  @Autowired
  public DiagnosisKeysStructureProvider(DiagnosisKeyService diagnosisKeyService, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, ExportBatchService exportBatchService) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.exportBatchService = exportBatchService;
  }

  /**
   * Get directory for diagnosis keys from database.
   * @return the directory
   */
  public Directory<WritableOnDisk> getDiagnosisKeys(ExportConfiguration exportConfiguration) {
    List<ExportBatch> exportBatches = buildExportBatches(exportConfiguration,
            getExportStartDateTime(exportConfiguration));

    List<Export> exports = new ArrayList<>();

    logger.debug("Created " + exportBatches.size() + " export batches");

    for (ExportBatch exportBatch : exportBatches) {
      exports.add(loadDiagnosisKeysForExportBatch(exportBatch));
    }

    return new DiagnosisKeysDirectory(exports, exportConfiguration, cryptoProvider, distributionServiceConfig);
  }

  private Export loadDiagnosisKeysForExportBatch(ExportBatch exportBatch) {
        Collection<DiagnosisKey> diagnosisKeys = diagnosisKeyService.getDiagnosisKeysBetween(
            exportBatch.getFromTimestamp().getEpochSecond() / 3600,
            exportBatch.getToTimestamp().getEpochSecond() / 3600);

        logger.debug("Loaded " + diagnosisKeys.size() + " diagnosis keys between " + exportBatch.getFromTimestamp()
                + " and " + exportBatch.getToTimestamp() + " from the database.");

        return new Export(new HashSet<>(diagnosisKeys), exportBatch);
  }

  // TODO: maybe error handling, if no diagnosis keys are found, if that is possible?

  /**
   * Loads the to timestamp of the latest export batch, that has been written to the database. This timestamp can
   * then be used to create new export batches, if there are no export batches available it will use the
   * submission timestamp from the oldest diagnosis keys, that is still newer then then from timestamp from
   * the {@link ExportConfiguration}.
   *
   * @return the timestamp, which should be used as the start of the export
   */
  private Instant getExportStartDateTime(ExportConfiguration exportConfiguration) {
    Optional<ExportBatch> tmp = this.exportBatchService.getLatestBatch(exportConfiguration.getId());
    return this.exportBatchService.getLatestBatch(exportConfiguration.getId())
            .map(ExportBatch::getToTimestamp)
            .orElseGet(() -> Instant.ofEpochSecond(this.diagnosisKeyService.getOldestDiagnosisKeyAfterTimestamp(
                    exportConfiguration.getFromTimestamp().getEpochSecond() / 3600).getSubmissionTimestamp()
                    * 3600));
  }

  /**
   * Builds the export batches, based on the export configuration and export start time.
   *
   * @param exportStart the time from which on batches should be created.
   * @return list of the export batches.
   */
  private List<ExportBatch> buildExportBatches(ExportConfiguration exportConfiguration, Instant exportStart) {
    ArrayList<ExportBatch> exportBatches = new ArrayList<>();
    // Prevents an unfinished period being created as an export batch
    Instant now = Instant.now().minus(exportConfiguration.getPeriod(), ChronoUnit.HOURS);
    // Since isBefore does not match on equal dates, the negation of isAfter needs to be used here
    while (!exportStart.isAfter(now)) {
      exportBatches.add(new ExportBatch(exportStart, exportStart.plus(
              exportConfiguration.getPeriod(), ChronoUnit.HOURS), ExportBatchStatus.OPEN,
              exportConfiguration));
      exportStart = exportStart.plus(exportConfiguration.getPeriod(), ChronoUnit.HOURS);
    }
//    exportBatchService.saveExportBatches(exportBatches);
    return exportBatches;
  }
}
