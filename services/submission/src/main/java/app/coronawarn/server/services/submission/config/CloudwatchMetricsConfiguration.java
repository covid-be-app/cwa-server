package app.coronawarn.server.services.submission.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Configuration
public class CloudwatchMetricsConfiguration {

    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchConfig config,
                                                           Clock clock, CloudWatchAsyncClient client) {
        return new CloudWatchMeterRegistry(config, clock, client);
    }

    @Bean
    public Clock micrometerClock() {
        return Clock.SYSTEM;
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder().build();
    }

  @Bean
  public CloudWatchConfig cloudWatchConfig(CloudWatchProperties properties) {
    return new CloudWatchConfig() {
      @Override
      public String prefix() {
        return null;
      }

      @Override
      public String namespace() {
        return properties.getNamespace();
      }

      @Override
      public Duration step() {
        return properties.getStep();
      }

      @Override
      public boolean enabled() {
        return properties.isEnabled();
      }

      @Override
      public int batchSize() {
        return properties.getBatchSize();
      }

      @Override
      public String get(String s) {
        return null;
      }
    };
  }

}
