/*
 * Coronalert / cwa-server
 *
 * (c) 2020 Devside SRL
 *
 * Deutsche Telekom AG and all other contributors /
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

package app.coronawarn.server.services.submission.covicode;

import app.coronawarn.server.common.persistence.domain.covicodes.CoviCode;
import app.coronawarn.server.common.persistence.repository.CoviCodeRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version/v1")
@Profile("allow-covicode-processing")
public class CoviCodeController {

  private static final Logger logger = LoggerFactory.getLogger(CoviCodeController.class);

  public static final String COVICODES_PATH = "/covicodes/{localDateStr}";

  private CoviCodeRepository coviCodeRepository;

  CoviCodeController(CoviCodeRepository coviCodeRepository) {
    this.coviCodeRepository = coviCodeRepository;
  }

  /**
   * Handles diagnosis key submission requests.
   *
   * @param localDateStr the date (UTC) for the covicodes
   * @return List of covicodes
   */
  @GetMapping(value = COVICODES_PATH)
  public ResponseEntity<List<CoviCode>> getCoviCodes(@PathVariable String localDateStr) {
    LocalDate localDate = LocalDate.parse(localDateStr);
    List<CoviCode> covis = coviCodeRepository
        .getCoviCodeByData(localDate.atStartOfDay(), localDate.atStartOfDay().plusDays(1));

    return ResponseEntity.ok(covis);
  }
}
