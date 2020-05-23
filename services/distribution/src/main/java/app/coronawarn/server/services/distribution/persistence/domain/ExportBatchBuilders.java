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

public interface ExportBatchBuilders {

  interface Builder {

    /**
     * Adds the specified from timestamp to this builder.
     *
     * @param fromTimestamp the timestamp as of which diagnosis keys should be considered.
     * @return this Builder instance.
     */
    ToTimeStampBuilder withFromTimestamp(Instant fromTimestamp);
  }

  interface ToTimeStampBuilder {

    /**
     * Adds the specified to timestamp to this builder.
     *
     * @param toTimestamp the timestamp up to which diagnosis keys should be considered.
     * @return this Builder instance.
     */
    StatusBuilder withToTimestamp(Instant toTimestamp);
  }

  interface StatusBuilder {

    /**
     * Adds the specified status to this builder.
     *
     * @param status the status of the export batch.
     * @return this Builder instance.
     */
    ExportConfigurationBuilder withStatus(ExportBatchStatus status);
  }

  interface ExportConfigurationBuilder {

    /**
     * Adds the specified export configuration to this builder.
     *
     * @param configuration the export configuration, which was used to create this export batch.
     * @return this Builder instance.
     */
    FinalBuilder withExportConfiguration(ExportConfiguration configuration);
  }

  interface FinalBuilder {

    /**
     * Builds a {@link ExportBatch} instance.
     */
    ExportBatch build();
  }

}
