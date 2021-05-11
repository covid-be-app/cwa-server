package app.coronawarn.server.services.submission.covicode;

import app.coronawarn.server.common.persistence.domain.covicodes.CoviCode;
import app.coronawarn.server.common.persistence.repository.CoviCodeRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("covicode-generator")
public class CoviCodeRunner {

  private static final Logger logger = LoggerFactory.getLogger(CoviCodeRunner.class);

  private final CoviCodeRepository coviCodeRepository;
  private final CoviCodeGenerator coviCodeGenerator;

  /**
   * Creates the CoviCodeRunner.
   */
  public CoviCodeRunner(CoviCodeRepository coviCodeRepository,
      CoviCodeGenerator coviCodeGenerator) {
    this.coviCodeRepository = coviCodeRepository;
    this.coviCodeGenerator = coviCodeGenerator;
  }

  /**
   * Fetch all ACs and transfer them to the submission server.
   */
  @Scheduled(fixedDelayString = "${services.submission.covicode.rate}")
  public void generateCoviCodes() throws Exception {
    logger.info("Generating and saving covicodes");

    persistCoviCodes(LocalDate.now());
    persistCoviCodes(LocalDate.now().plusDays(1));
    persistCoviCodes(LocalDate.now().plusDays(2));

  }

  private void persistCoviCodes(LocalDate localDate) throws Exception {
    List<CoviCode> coviCodes = this.coviCodeGenerator.generateCoviCodes(localDate);

    logger.info("Persisting " + coviCodes.size() + " covicodes for start "
        + coviCodes.get(0).getStartInterval() + " to end " + coviCodes.get(coviCodes.size() - 1).getEndInterval());

    coviCodes.forEach(code ->
        coviCodeRepository.saveDoNothingOnConflict(
            code.getCode(),
            code.getStartInterval(),
            code.getEndInterval()
        )
    );

    logger.info("Done persisting");
  }
}
