package app.coronawarn.server.common.persistence.domain.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validator for the ISO country.
 * https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = CountryIso3166Validator.class)
@Documented
public @interface CountryIso3166 {

  /**
   * The validation constraint message.
   */
  String message() default "{validation.constraints.country.message}";

  /**
   * The groups.
   */
  Class<?>[] groups() default {};

  /**
   * The payload.
   */
  Class<? extends Payload>[] payload() default {};
}
