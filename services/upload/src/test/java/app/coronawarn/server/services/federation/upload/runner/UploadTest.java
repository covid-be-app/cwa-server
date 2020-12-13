

package app.coronawarn.server.services.federation.upload.runner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.federation.upload.client.TestFederationUploadClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;

//TODO: this test crashed the JVM locally ... why ....
//@EnableConfigurationProperties(value = UploadServiceConfig.class)
//@ActiveProfiles({"testdata", "fake-client"})
//@DirtiesContext
//@SpringBootTest
class UploadTest {

  @SpyBean
  TestFederationUploadClient spyFederationClient;

  @Test
  @Disabled
  void shouldGenerateTestKeys() {
    verify(spyFederationClient, times(2)).postBatchUpload(any());
  }

}
