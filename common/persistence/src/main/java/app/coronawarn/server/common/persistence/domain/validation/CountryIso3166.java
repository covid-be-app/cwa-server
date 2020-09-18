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
