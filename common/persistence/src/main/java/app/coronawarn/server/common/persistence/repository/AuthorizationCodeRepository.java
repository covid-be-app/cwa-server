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

package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.authorizationcode.AuthorizationCode;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationCodeRepository extends PagingAndSortingRepository<AuthorizationCode, Long> {

  @Modifying
  @Query("INSERT INTO authorization_code "
      + "(signature, mobile_test_id, date_patient_infectious, date_test_communicated) "
      + "VALUES (:signature, :mobile_test_id, :date_patient_infectious, :date_test_communicated) "
      + "ON CONFLICT DO NOTHING")
  void saveDoNothingOnConflict(
      @Param("signature") String signature,
      @Param("mobile_test_id") String mobileTestId,
      @Param("date_patient_infectious") LocalDate datePatientInfectious,
      @Param("date_test_communicated") LocalDate dateTestCommunicated
  );

  /**
   * Lookup old obsolete authorization codes for cleanup.
   *
   * @param before all ACs older than the date provided that are to be deleted.
   * @return
   */
  @Modifying
  @Query("DELETE FROM authorization_code WHERE date_patient_infectious <= :before")
  Integer deleteObsoleteAuthorizationCodes(@Param("before") LocalDate before);

  Optional<AuthorizationCode> findByMobileTestIdAndDatePatientInfectious(
      String mobileTestId, LocalDate datePatientInfectious);
}
