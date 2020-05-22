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

import app.coronawarn.server.services.distribution.persistence.domain.ExportConfiguration;
import app.coronawarn.server.services.distribution.persistence.repository.ExportConfigurationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExportConfigurationService {

  private final ExportConfigurationRepository exportConfigurationRepository;

  @Autowired
  public ExportConfigurationService(ExportConfigurationRepository exportConfigurationRepository) {
    this.exportConfigurationRepository = exportConfigurationRepository;
  }

  /**
   * Returns a list of all export configurations on the database.
   *
   * @return List of export configurations.
   */
  public List<ExportConfiguration> getExportConfigurations() {
    return exportConfigurationRepository.findAll();
  }
}
