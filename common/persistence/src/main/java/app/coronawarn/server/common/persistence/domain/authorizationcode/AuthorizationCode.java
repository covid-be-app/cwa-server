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

package app.coronawarn.server.common.persistence.domain.authorizationcode;

import java.time.LocalDate;

/**
 * This class represents the authorization code.
 */
public class AuthorizationCode {

  private String signature;
  private String mobileTestId;
  private LocalDate datePatientInfectious;
  private LocalDate dateTestCommunicated;

  public AuthorizationCode() {

  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public String getMobileTestId() {
    return mobileTestId;
  }

  public void setMobileTestId(String mobileTestId) {
    this.mobileTestId = mobileTestId;
  }

  public LocalDate getDatePatientInfectious() {
    return datePatientInfectious;
  }

  public void setDatePatientInfectious(LocalDate datePatientInfectious) {
    this.datePatientInfectious = datePatientInfectious;
  }

  public LocalDate getDateTestCommunicated() {
    return dateTestCommunicated;
  }

  public void setDateTestCommunicated(LocalDate dateTestCommunicated) {
    this.dateTestCommunicated = dateTestCommunicated;
  }

  @Override
  public String toString() {
    return "AuthorizationCode{" +
        "signature='" + signature + '\'' +
        ", mobileTestId='" + mobileTestId + '\'' +
        ", datePatientInfectious=" + datePatientInfectious +
        ", dateTestCommunicated=" + dateTestCommunicated +
        '}';
  }
}
