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

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory;

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.Export;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.archive.decorator.singing.DiagnosisKeySigningDecorator;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file.TemporaryExposureKeyExportFile;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import java.util.Set;

public class DiagnosisKeysExportBatchDirectory extends IndexDirectoryOnDisk<Export> {

  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysExportBatchDirectory} instance for the specified date.
   *
   * @param exports A collection of exports, which contains the diagnosis keys, that should be written to storage.
   * @param cryptoProvider The {@link CryptoProvider} used for cryptographic signing.
   */
  public DiagnosisKeysExportBatchDirectory(Collection<Export> exports, CryptoProvider cryptoProvider,
                                           DistributionServiceConfig distributionServiceConfig) {
    // TODO: probably needs to be updated, i.e. daily for 24 hours
    super(distributionServiceConfig.getApi().getHourPath(),
        indices -> Set.copyOf(exports), exportFormatter -> exportFormatter.getBatch().getFromTimestamp().toString());
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      Export export = (Export) currentIndices.peek();

      File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
              export.getKeys(), export.getBatch().getConfiguration().getRegion(),
              export.getBatch().getFromTimestamp().getEpochSecond(),
              export.getBatch().getToTimestamp().getEpochSecond(), distributionServiceConfig);

      Archive<WritableOnDisk> exportArchive = new ArchiveOnDisk(distributionServiceConfig.getOutputFileName());
      exportArchive.addWritable(temporaryExposureKeyExportFile);

      return decorateDiagnosisKeyArchive(exportArchive);
    });
    super.prepare(indices);
  }

  private Directory<WritableOnDisk> decorateDiagnosisKeyArchive(Archive<WritableOnDisk> archive) {
    return new DiagnosisKeySigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }
}
