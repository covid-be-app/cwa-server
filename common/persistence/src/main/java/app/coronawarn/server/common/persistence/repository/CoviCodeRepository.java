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

import app.coronawarn.server.common.persistence.domain.covicodes.CoviCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoviCodeRepository extends PagingAndSortingRepository<CoviCode, String> {

  @Modifying
  @Query("INSERT INTO covi_code "
      + "(code, start_interval, end_interval) "
      + "VALUES (:code, :startInterval, :endInterval) "
      + "ON CONFLICT DO NOTHING")
  void saveDoNothingOnConflict(
      @Param("code") String code,
      @Param("startInterval") LocalDateTime startInterval,
      @Param("endInterval") LocalDateTime endInterval
  );

  @Query("SELECT * FROM covi_code WHERE start_interval>=:startInterval AND end_interval<=:endInterval")
  List<CoviCode> getCoviCodeByData(
      @Param("startInterval") LocalDateTime startInterval,
      @Param("endInterval") LocalDateTime endInterval);


  @Modifying
  @Query("DELETE FROM covi_code WHERE end_interval<:threshold")
  void deleteOlderThan(@Param("threshold") LocalDate date);
}
