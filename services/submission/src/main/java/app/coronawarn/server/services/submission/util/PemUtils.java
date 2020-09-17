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

package app.coronawarn.server.services.submission.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PublicKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class PemUtils {

  /**
   * Retries the public key from a string (used when SecureString resources are fetched via SSM).
   *
   * @param pemAsString The content of the pem as a string
   * @return a public key
   * @throws IOException in case of an exception
   */
  public static PublicKey getPublicKeyFromString(String pemAsString) throws IOException {
    ByteArrayInputStream pemInputStream = new ByteArrayInputStream(pemAsString.getBytes());
    Reader reader = new BufferedReader(new InputStreamReader(pemInputStream));

    Object parsed = new PEMParser(reader).readObject();
    return new JcaPEMKeyConverter().getPublicKey((SubjectPublicKeyInfo) parsed);
  }


}
