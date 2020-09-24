package app.coronawarn.server.services.submission.config;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "management.metrics.export.cloudwatch")
public class CloudWatchProperties extends StepRegistryProperties {

  private String namespace = "";
  private boolean enabled = true;

  public String getNamespace() {
    return this.namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
