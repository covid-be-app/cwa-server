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

package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.persistence.domain.ExportBatch;
import app.coronawarn.server.services.distribution.persistence.domain.ExportBatchStatus;
import app.coronawarn.server.services.distribution.persistence.domain.ExportConfiguration;
import app.coronawarn.server.services.distribution.persistence.service.ExportBatchService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * This runner assembles and writes diagnosis key bundles and the parameter configuration.
 */
public class Assembly {

  private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

  private final OutputDirectoryProvider outputDirectoryProvider;

  private final CwaApiStructureProvider cwaApiStructureProvider;

  private final ExportBatchService exportBatchService;

  private final ExportConfiguration exportConfiguration;

  private final DiagnosisKeyService diagnosisKeyService;

  private final ApplicationContext applicationContext;

  /**
   * Creates an Assembly, using {@link OutputDirectoryProvider}, {@link CwaApiStructureProvider} and
   * {@link ApplicationContext}.
   */
  public Assembly(OutputDirectoryProvider outputDirectoryProvider, CwaApiStructureProvider cwaApiStructureProvider,
                  ExportBatchService exportBatchService, ExportConfiguration exportConfiguration,
                  DiagnosisKeyService diagnosisKeyService, ApplicationContext applicationContext) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.cwaApiStructureProvider = cwaApiStructureProvider;
    this.exportBatchService = exportBatchService;
    this.exportConfiguration = exportConfiguration;
    this.diagnosisKeyService = diagnosisKeyService;
    this.applicationContext = applicationContext;
  }



  /**
   * Starts the Assembly runner.
   */
  public void run() {
    try {
      Directory<WritableOnDisk> outputDirectory = this.outputDirectoryProvider.getDirectory();

      outputDirectory.addWritable(cwaApiStructureProvider.getDirectory(exportConfiguration));
      this.outputDirectoryProvider.clear();
      logger.debug("Preparing files...");
      outputDirectory.prepare(new ImmutableStack<>());
      // TODO update batches once finished
      // FIXME this currently throws errors, since the index files are already there
      logger.debug("Writing files...");
      outputDirectory.write();
    } catch (Exception e) {
      logger.error("Distribution data assembly failed.", e);
      Application.killApplication(applicationContext);
    }

    logger.debug("Distribution data assembled successfully.");
  }
}
