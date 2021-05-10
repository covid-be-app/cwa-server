/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * All modifications are copyright (c) 2020 Devside SRL.
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.common.persistence.domain.covicodes;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.covicodes.CoviCodesBuilders.FinalBuilder;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this builder can be retrieved by calling {@link DiagnosisKey#builder()}. A {@link DiagnosisKey} can
 * then be build by either providing the required member values or by passing the respective protocol buffer object.
 */
public class CoviCodeBuilder implements
    FinalBuilder {

  private static final Logger logger = LoggerFactory.getLogger(CoviCodeBuilder.class);

  private String code;
  private LocalDateTime startInterval;
  private LocalDateTime endInterval;
  private CoviCodeStatus status;

  CoviCodeBuilder() {
  }

  @Override
  public FinalBuilder withCode(String code) {
    this.code = code;
    return this;
  }

  @Override
  public FinalBuilder withStartInterval(LocalDateTime startInterval) {
    this.startInterval = startInterval;
    return this;
  }

  @Override
  public FinalBuilder withEndInterval(LocalDateTime endInterval) {
    this.endInterval = endInterval;
    return this;
  }

  @Override
  public FinalBuilder withCoviCodeStatus(CoviCodeStatus status) {
    this.status = status;
    return this;
  }

  @Override
  public CoviCode build() {
    CoviCode coviCode = new CoviCode(
        this.code,
        this.startInterval,
        this.endInterval,
        CoviCodeStatus.CREATED
    );

    return coviCode;
  }
}
