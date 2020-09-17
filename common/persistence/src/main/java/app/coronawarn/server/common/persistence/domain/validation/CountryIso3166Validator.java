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
