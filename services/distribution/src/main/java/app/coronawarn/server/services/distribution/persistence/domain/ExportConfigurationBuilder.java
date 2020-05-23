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

import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.AppPkgIdBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.Builder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.BundleIdBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.FilenameRootBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.FinalBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.FromTimestampBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.PeriodBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.RegionBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.SigningKeyBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.SigningKeyIdBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.SigningKeyVersionBuilder;
import static app.coronawarn.server.services.distribution.persistence.domain.ExportConfigurationBuilders.ThruTimestampBuilder;

import app.coronawarn.server.common.persistence.domain.DiagnosisKeyBuilder;
import app.coronawarn.server.services.distribution.persistence.exception.InvalidExportConfigurationException;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExportConfigurationBuilder implements Builder, FilenameRootBuilder, PeriodBuilder, RegionBuilder,
    FromTimestampBuilder, ThruTimestampBuilder, SigningKeyBuilder, SigningKeyIdBuilder, SigningKeyVersionBuilder,
    AppPkgIdBuilder, BundleIdBuilder,FinalBuilder {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBuilder.class);

  private String bucketName;
  private String filenameRoot;
  private int period;
  private String region;
  private Instant fromTimestamp;
  private Instant thruTimestamp;
  private String signingKey;
  private String signingKeyId;
  private String signingKeyVersion;
  private String appPkgId;
  private String bundleId;

  ExportConfigurationBuilder() {
  }

  @Override
  public FilenameRootBuilder withBucketName(String bucketName) {
    this.bucketName =  bucketName;
    return this;
  }

  @Override
  public PeriodBuilder withFilenameRoot(String filenameRoot) {
    this.filenameRoot = filenameRoot;
    return this;
  }

  @Override
  public RegionBuilder withPeriod(int period) {
    this.period = period;
    return this;
  }

  @Override
  public FromTimestampBuilder withRegion(String region) {
    this.region = region;
    return this;
  }

  @Override
  public ThruTimestampBuilder withFromTimestamp(Instant fromTimestamp) {
    this.fromTimestamp = fromTimestamp;
    return this;
  }

  @Override
  public SigningKeyBuilder withThruTimestamp(Instant thruTimestamp) {
    this.thruTimestamp = thruTimestamp;
    return this;
  }

  @Override
  public SigningKeyIdBuilder withSigningKey(String signingKey) {
    this.signingKey = signingKey;
    return this;
  }

  @Override
  public SigningKeyVersionBuilder withSigningKeyId(String signingKeyId) {
    this.signingKeyId = signingKeyId;
    return this;
  }

  @Override
  public AppPkgIdBuilder withSigningKeyVersion(String signingKeyVersion) {
    this.signingKeyVersion = signingKeyVersion;
    return this;
  }

  @Override
  public BundleIdBuilder withAppPkgId(String appPkgId) {
    this.appPkgId = appPkgId;
    return this;
  }

  @Override
  public FinalBuilder withBundleId(String bundleId) {
    this.bundleId = bundleId;
    return this;
  }

  @Override
  public ExportConfiguration build() {
    ExportConfiguration exportConfiguration = new ExportConfiguration(this.bucketName, this.filenameRoot, this.period,
            this.region, this.fromTimestamp, this.thruTimestamp, this.signingKey, this.signingKeyId,
            this.signingKeyVersion, this.appPkgId, this.bundleId);

    return throwValidationFails(exportConfiguration);
  }

  private ExportConfiguration throwValidationFails(ExportConfiguration exportConfiguration) {
    Set<ConstraintViolation<ExportConfiguration>> violations = exportConfiguration.getConstraintViolations();

    if (!violations.isEmpty()) {
      String violationsMessage = violations.stream()
            .map(violation -> String.format("%s Invalid Value: %s", violation.getMessage(),
                    violation.getInvalidValue()))
            .collect(Collectors.toList()).toString();
      logger.debug(violationsMessage);
      throw new InvalidExportConfigurationException(violationsMessage);
    }

    return exportConfiguration;
  }
}
