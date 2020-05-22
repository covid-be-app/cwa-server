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

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.ExportBatchWithKeys;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.decorator.DiagnosisKeySigningDecorator;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file.TemporaryExposureKeyExportFile;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.util.Collection;
import java.util.Set;

public class DiagnosisKeysExportBatchDirectory extends IndexDirectoryOnDisk<ExportBatchWithKeys> {

  private static final String DATE_DIRECTORY = "date";

  //private final ExportBatchWithKeys diagnosisKeys;

  private final CryptoProvider cryptoProvider;

  /**
   * Constructs a {@link DiagnosisKeysExportBatchDirectory} instance for the specified date.
   *
   * @param diagnosisKeys A collection of diagnosis keys. These will be filtered according to the specified current
   *        date.
   * @param cryptoProvider The {@link CryptoProvider} used for cryptographic signing.
   */
  public DiagnosisKeysExportBatchDirectory(Collection<ExportBatchWithKeys> diagnosisKeys,
                                           CryptoProvider cryptoProvider) {
    super(DATE_DIRECTORY, __ -> Set.copyOf(diagnosisKeys), a -> a);

    //this.diagnosisKeys = diagnosisKeys;
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.addWritableToAll(currentIndices -> {
      ExportBatchWithKeys batchWithKeys = (ExportBatchWithKeys) currentIndices.peek();

      String region = (String) currentIndices.pop().pop().peek();
      Set<DiagnosisKey> diagnosisKeysForCurrentHour = batchWithKeys.getKeys();

      long startTimestamp = batchWithKeys.getFromTimestamp().getEpochSecond();
      long endTimestamp = batchWithKeys.getToTimestamp().getEpochSecond();
      File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
          diagnosisKeysForCurrentHour, region, startTimestamp, endTimestamp);
      Archive<WritableOnDisk> hourArchive = new ArchiveOnDisk("index");
      hourArchive.addWritable(temporaryExposureKeyExportFile);
      return decorateDiagnosisKeyArchive(hourArchive);
    });
    super.prepare(indices);
  }

  private Archive<WritableOnDisk> createContent() {
    //LocalDateTime currentHour = (LocalDateTime) currentIndices.peek();
    // The LocalDateTime currentHour already contains both the date and the hour information, so
    // we can throw away the LocalDate that's the second item on the stack from the "/date"
    // IndexDirectory.
    //String region = (String) currentIndices.pop().pop().peek();

    String region = "DE";

    Set<DiagnosisKey> diagnosisKeysForCurrentHour = null; //exportBatch.getDiagnosisKeys();

    long startTimestamp = 0L;//currentHour.toEpochSecond(ZoneOffset.UTC);
    long endTimestamp = 0L;//currentHour.plusHours(1).toEpochSecond(ZoneOffset.UTC);
    File<WritableOnDisk> temporaryExposureKeyExportFile = TemporaryExposureKeyExportFile.fromDiagnosisKeys(
        diagnosisKeysForCurrentHour, region, startTimestamp, endTimestamp);

    Archive<WritableOnDisk> hourArchive = new ArchiveOnDisk("index");
    hourArchive.addWritable(temporaryExposureKeyExportFile);

    return hourArchive;
  }

  private Directory<WritableOnDisk> decorateDiagnosisKeyArchive(Archive<WritableOnDisk> archive) {
    return new DiagnosisKeySigningDecorator(archive, cryptoProvider);
  }
}
