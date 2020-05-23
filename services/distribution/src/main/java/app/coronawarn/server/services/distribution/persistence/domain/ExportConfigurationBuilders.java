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

public interface ExportConfigurationBuilders {

  interface Builder {

    /**
     * Adds the specified bucketName to this builder.
     *
     * @param bucketName the bucket name.
     * @return this Builder instance.
     */

    FilenameRootBuilder withBucketName(String bucketName);
  }

  interface FilenameRootBuilder {

    /**
     * Adds the specified filenameRoot to this builder.
     *
     * @param filenameRoot the filename root.
     * @return this Builder instance.
     */

    PeriodBuilder withFilenameRoot(String filenameRoot);
  }

  interface PeriodBuilder {

    /**
     * Adds the specified period to this builder.
     *
     * @param period the interval in which files should be created.
     * @return this Builder instance.
     */
    RegionBuilder withPeriod(int period);
  }

  interface RegionBuilder {

    /**
     * Adds the specified region to this builder.
     *
     * @param region the region, which should be used during the export.
     * @return this Builder instance.
     */

    FromTimestampBuilder withRegion(String region);
  }

  interface FromTimestampBuilder {

    /**
     * Adds the specified from timestamp to this builder.
     *
     * @param fromTimestamp the timestamp as of which diagnosis keys should be considered.
     * @return this Builder instance.
     */

    ThruTimestampBuilder withFromTimestamp(Instant fromTimestamp);
  }

  interface ThruTimestampBuilder  {

    /**
     * Adds the specified thru timestamp to this builder.
     *
     * @param thruTimestamp the timestamp up to which diagnosis keys should be considered.
     * @return this Builder instance.
     */

    SigningKeyBuilder withThruTimestamp(Instant thruTimestamp);
  }

  interface SigningKeyBuilder {

    /**
     * Adds the specified signing key to this builder.
     *
     * @param signingKey the signing key, that should be used.
     * @return this Builder instance.
     */

    SigningKeyIdBuilder withSigningKey(String signingKey);
  }

  interface SigningKeyIdBuilder {

    /**
     * Adds the specified signing key id to this builder.
     *
     * @param signingKeyId the signing key id.
     * @return this Builder instance.
     */

    SigningKeyVersionBuilder withSigningKeyId(String signingKeyId);
  }

  interface SigningKeyVersionBuilder {

    /**
     * Adds the specified signing key version to this builder.
     *
     * @param signingKeyVersion the signing key version.
     * @return this Builder instance.
     */

    AppPkgIdBuilder withSigningKeyVersion(String signingKeyVersion);
  }

  interface AppPkgIdBuilder {

    /**
     * Adds the specified app package id to this builder.
     *
     * @param appPkgId the app package id.
     * @return this Builder instance.
     */

    BundleIdBuilder withAppPkgId(String appPkgId);
  }

  interface BundleIdBuilder {

    /**
     * Adds the specified bundle id key to this builder.
     *
     * @param bundleId the bundle id.
     * @return this Builder instance.
     */

    FinalBuilder withBundleId(String bundleId);
  }

  interface FinalBuilder {

    /**
     * Builds a {@link ExportConfiguration} instance.
     */
    ExportConfiguration build();

  }

}
