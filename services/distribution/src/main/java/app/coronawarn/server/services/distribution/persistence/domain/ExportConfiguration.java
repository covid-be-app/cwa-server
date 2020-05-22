package app.coronawarn.server.services.distribution.persistence.domain;

import java.time.Instant;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;

@Entity
@Table(name = "export_configuration")
public class ExportConfiguration {
  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String bucketName;

  private String filenameRoot;

  @Min(value = 1, message = "Period must be greater than 0.")
  private int period;

  private String region;

  private Instant fromTimestamp;

  private Instant thruTimestamp;

  private String signingKey;

  private String signingKeyId;

  private String signingKeyVersion;

  private String appPkgId;

  private String bundleId;

  protected ExportConfiguration() {
  }

  // TODO: just temporarily

  /**
   * tmp javadoc.
   */
  public ExportConfiguration(String bucketName, String filenameRoot, int period, String region,
                      Instant fromTimestamp, Instant thruTimestamp, String signingKey, String signingKeyId,
                      String signingKeyVersion, String appPkgId, String bundleId) {
    this.bucketName = bucketName;
    this.filenameRoot = filenameRoot;
    this.period = period;
    this.region = region;
    this.fromTimestamp = fromTimestamp;
    this.thruTimestamp = thruTimestamp;
    this.signingKey = signingKey;
    this.signingKeyId = signingKeyId;
    this.signingKeyVersion = signingKeyVersion;
    this.appPkgId = appPkgId;
    this.bundleId = bundleId;
  }

  /**
   * Returns the configuration id.
   */
  public Long getId() {
    return id;
  }

  /**
   * Returns the bucket name, in which the diagnosis keys should be stored.
   */
  public String getBucketName() {
    return bucketName;
  }

  /**
   * Returns the parent directory name.
   */
  public String getFilenameRoot() {
    return filenameRoot;
  }

  /**
   *  Returns the interval to export data to S3 in hours.
   */
  public int getPeriod() {
    return period;
  }

  /**
   * Returns the region, from which the diagnosis keys should be considered, i.e. DE, CA.
   */
  public String getRegion() {
    return region;
  }

  /**
   * Returns the timestamp from which the configuration is active.
   */
  public Instant getFromTimestamp() {
    return fromTimestamp;
  }

  /**
   * Returns the timestamp to which the configuration is active.
   */
  public Instant getThruTimestamp() {
    return thruTimestamp;
  }

  /**
   * Returns the KMS resource id, which is used for signing.
   */
  public String getSigningKey() {
    return signingKey;
  }

  /**
   * Returns the ID of the signing key (for clients).
   */
  public String getSigningKeyId() {
    return signingKeyId;
  }

  /**
   * Returns the version of the signing key (for clients).
   */
  public String getSigningKeyVersion() {
    return signingKeyVersion;
  }

  /**
   * Returns the app package id to put in export headers.
   */
  public String getAppPkgId() {
    return appPkgId;
  }

  /**
   * Returns the bundle ID to put in export headers.
   */
  public String getBundleId() {
    return bundleId;
  }

  /**
   * Determines if this configuration is valid.
   *
   * @return true, if valid, else false.
   */
  public boolean isValid() {
    Set<ConstraintViolation<ExportConfiguration>> violations = getConstraintViolations();
    return violations.isEmpty();
  }

  /**
   * Gets any constraint violations that this key might incorporate.
   *
   * <ul>
   * <li>period must be greater than 0</li>
   * </ul>
   *
   * @return A set of constraint violations of this key.
   */
  public Set<ConstraintViolation<ExportConfiguration>> getConstraintViolations() {
    return VALIDATOR.validate(this);
  }

  /**
   * Checks whether the current time is between fromTimestamp and thruTimestamp.
   *
   * @return true, if the config is active, otherwise false
   */
  public boolean isActive() {
    Instant now = Instant.now();
    return this.fromTimestamp.isBefore(now) && this.thruTimestamp.isAfter(now);
  }
}
