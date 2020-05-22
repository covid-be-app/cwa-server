package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.assembly.component.CwaApiStructureProvider;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.persistence.domain.ExportConfiguration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * This runner reads the configurations and spawns one {@link Assembly} runner for each active configuration.
 */
@Component
@Order(2)
public class ExportRunner implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

  private final OutputDirectoryProvider outputDirectoryProvider;

  private final CwaApiStructureProvider cwaApiStructureProvider;

  private final ApplicationContext applicationContext;

  private ArrayList<Thread> threads = new ArrayList<>();

  /**
   * Creates an ExportRunner Runner, using {@link OutputDirectoryProvider}, {@link CwaApiStructureProvider}
   * and {@link ApplicationContext}.
   */
  @Autowired
  public ExportRunner(OutputDirectoryProvider outputDirectoryProvider, CwaApiStructureProvider cwaApiStructureProvider,
                  ApplicationContext applicationContext) {
    this.outputDirectoryProvider = outputDirectoryProvider;
    this.cwaApiStructureProvider = cwaApiStructureProvider;
    this.applicationContext = applicationContext;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      ExportConfiguration[] configurations = this.loadConfigurations();
      logger.debug("Loaded " + configurations.length + " configurations.");

      for (ExportConfiguration configuration: configurations) {
        if (configuration.isActive()) {
          Assembly assemblyRunner = new Assembly(this.outputDirectoryProvider, this.cwaApiStructureProvider,
                  configuration, this.applicationContext);
          Thread thread = new Thread(assemblyRunner);
          threads.add(thread);
          thread.start();
        }
      }
    } catch (Exception e) {
      logger.error("ExportRunner Runner failed.", e);
      for (Thread thread: threads) {
        thread.interrupt();
      }
      Application.killApplication(applicationContext);
    }
  }

  /**
   * Loads the configuration for the assembly runner from the database.
   * @return the configurations for the assembly runner
   */
  public ExportConfiguration[] loadConfigurations() {
    // TODO: tmp implementation, until db is connected
    Instant start = Instant.now().minus(20, ChronoUnit.DAYS);
    Instant end = Instant.now().plus(20, ChronoUnit.DAYS);
    ExportConfiguration tmp1 =
            new ExportConfiguration("tmp1", "test", 1, "DE", start, end,
                    "abc", "abc", "abc", "abc", "abc");

    ExportConfiguration tmp2 =
            new ExportConfiguration("tmp2", "test", 5, "DE", end, start,
                    "abc", "abc", "abc", "abc", "abc");

    ExportConfiguration tmp3 =
            new ExportConfiguration("tmp3", "test", 5, "DE", start, end,
                    "abc", "abc", "abc", "abc", "abc");

    ExportConfiguration[] out = new ExportConfiguration[3];
    out[0] = tmp1;
    out[1] = tmp2;
    out[2] = tmp3;

    return out;
  }
}
