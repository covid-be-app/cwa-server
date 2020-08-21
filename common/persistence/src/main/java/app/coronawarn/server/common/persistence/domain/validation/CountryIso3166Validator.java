package app.coronawarn.server.common.persistence.domain.validation;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.Locale;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CountryIso3166Validator implements ConstraintValidator<CountryIso3166, String> {

  /**
   * Validates the value to check if it is a valid country code.
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return StringUtils.isBlank(value) || Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3)
        .contains(value.toUpperCase());
  }
}
