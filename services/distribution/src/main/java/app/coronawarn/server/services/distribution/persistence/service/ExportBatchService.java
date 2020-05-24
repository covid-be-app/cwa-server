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

package app.coronawarn.server.services.distribution.persistence.service;

import app.coronawarn.server.services.distribution.persistence.domain.ExportBatch;
import app.coronawarn.server.services.distribution.persistence.domain.ExportConfiguration;
import app.coronawarn.server.services.distribution.persistence.repository.ExportBatchRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

@Component
public class ExportBatchService {

  private final ExportBatchRepository exportBatchRepository;

   @Autowired
  public ExportBatchService(ExportBatchRepository exportBatchRepository) {
    this.exportBatchRepository = exportBatchRepository;
  }

  /**
   * Saves the export batches to the database.
   *
   * @param exportBatches The export batches, that needs to be saved
   */
  public void saveExportBatches(List<ExportBatch> exportBatches) {
    exportBatchRepository.saveAll(exportBatches);
  }

  /**
   * Returns all export batches saved on the database.
   *
   * @return List of all export batches.
   */
  public List<ExportBatch> getExportBatches() {
    return exportBatchRepository.findAll();
  }

  /**
   * Returns the latest export batch by toTimestamp saved on the database.
   *
   * @return List of all export batches.
   */
  public Optional<ExportBatch> getLatestBatch(Long configurationId) {
    return exportBatchRepository.findFirstByConfigurationIdOrderByToTimestampDesc(configurationId);
  }

}
