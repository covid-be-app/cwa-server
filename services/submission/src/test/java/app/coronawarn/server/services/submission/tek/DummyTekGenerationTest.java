/*
 * Coronalert / cwa-testresult-server
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

package app.coronawarn.server.services.submission.tek;

import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import io.reactivex.Single;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
    "services.submission.tek.dummy.min-range=1",
    "services.submission.tek.dummy.max-range=1",
    "services.submission.tek.dummy.rate=1000"
  }
)
@ActiveProfiles({"dummy-tek-generation"})
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class DummyTekGenerationTest {

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @Test
  @Disabled //TODO: test why cleanup is not happning
  public void dummyKeysAreGenerated() {
    // prepare
    diagnosisKeyRepository.deleteAll();

    Iterable<DiagnosisKey> allKeys = null;
    allKeys = diagnosisKeyRepository.findAll();

    assertThat(IterableUtil.sizeOf(allKeys)).isEqualTo(0);
    // wait
    Single.fromCallable(() -> true).delay(2, TimeUnit.SECONDS).blockingGet();

    allKeys = diagnosisKeyRepository.findAll();
    assertThat(IterableUtil.sizeOf(allKeys)).isEqualTo(2);

    // cleanup
    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .plusHours(1)
        .toEpochSecond(UTC) / SECONDS_PER_HOUR;
    diagnosisKeyRepository.deleteOlderThan(threshold);

    allKeys = diagnosisKeyRepository.findAll();
    assertThat(IterableUtil.sizeOf(allKeys)).isEqualTo(0);

  }
}
