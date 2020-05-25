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
import java.util.Set;

public class DiagnosisKeysExportBatchDirectory extends IndexDirectoryOnDisk<Export> {

  private final Export export;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Constructs a {@link DiagnosisKeysExportBatchDirectory} instance for the specified date.
   *
   * @param export A collection of diagnosis keys. These will be filtered according to the specified current
   *        date.
   * @param cryptoProvider The {@link CryptoProvider} used for cryptographic signing.
   */
  public DiagnosisKeysExportBatchDirectory(Export export, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    // "hour" created here
    super(distributionServiceConfig.getApi().getHourPath(),
        indices -> Set.of(export), exportFormatter -> exportFormatter.getBatch().getFromTimestamp().toString());
    this.export = export;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
            export.getKeys(), export.getBatch().getConfiguration().getRegion(),
            export.getBatch().getFromTimestamp().getEpochSecond(), export.getBatch().getToTimestamp().getEpochSecond(),
            distributionServiceConfig);
    Archive<WritableOnDisk> exportBatchArchive = new ArchiveOnDisk("index");
    exportBatchArchive.addWritable(temporaryExposureKeyExportFile);
    this.addWritable(decorateDiagnosisKeyArchive(exportBatchArchive));
    super.prepare(indices);



//
//    this.addWritableToAll(currentIndices -> {
//      Export batchWithKeys = (Export) currentIndices.peek();
//
//      String region = (String) currentIndices.pop().peek();
//
//      long startTimestamp = batchWithKeys.getBatch().getFromTimestamp().getEpochSecond();
//      long endTimestamp = batchWithKeys.getBatch().getToTimestamp().getEpochSecond();
//      File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
//          export.getKeys(), region, startTimestamp, endTimestamp, distributionServiceConfig);
//      Archive<WritableOnDisk> exportBatchArchive = new ArchiveOnDisk("index");
//      exportBatchArchive.addWritable(temporaryExposureKeyExportFile);
//      return decorateDiagnosisKeyArchive(exportBatchArchive);
//    });
    super.prepare(indices);
  }

  private Directory<WritableOnDisk> decorateDiagnosisKeyArchive(Archive<WritableOnDisk> archive) {
    return new DiagnosisKeySigningDecorator(archive, cryptoProvider, distributionServiceConfig);
  }
}
