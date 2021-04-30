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

package app.coronawarn.server.common.persistence.utils;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtils {

  public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

  public static final String TEXT = "TEST REQUEST";

  private final PublicKey publicKey;

  /**
   * Creates an instance of the CryptoUtils.
   * Perform tasks like:
   * - R1 generation (during TEK submission)
   * - TEK key AC signature validation
   */
  public CryptoUtils(String publicKeyContent) throws IOException {
    this.publicKey = PemUtils.getPublicKeyFromString(publicKeyContent);
    Security.addProvider(new BouncyCastleProvider());

  }

  /**
   * Decode a given AES key.
   */
  public static SecretKey decodeAesKey(String base64Encodedkey) throws NoSuchAlgorithmException {
    byte[] decodedBytes = Base64.getDecoder().decode(base64Encodedkey.getBytes());
    return new SecretKeySpec(decodedBytes, 0, decodedBytes.length, "AES");
  }

  /**
   * Generate a hash.
   */
  public static byte[] generateHash(String message, SecretKey secretKey) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(secretKey);
    return mac.doFinal(message.getBytes());
  }

  /**
   * Verifies the signature using the given public key, the data and the signature.
   *
   * @param data           the data where the signature was applied on
   * @param signatureAsHex the signature in hex format
   * @return boolena indicating if signature was valid or not.
   * @throws Exception in case something goes wrong.
   */
  public boolean verify(String data, String signatureAsHex) throws Exception {
    byte[] signatureBytes = parseHexBinary(signatureAsHex);
    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initVerify(this.publicKey);
    signature.update(data.getBytes());
    return signature.verify(signatureBytes);
  }

  private byte[] parseHexBinary(String s) {
    final int len = s.length();

    // "111" is not a valid hex encoding.
    if (len % 2 != 0) {
      throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);
    }

    byte[] out = new byte[len / 2];

    for (int i = 0; i < len; i += 2) {
      int h = hexToBin(s.charAt(i));
      int l = hexToBin(s.charAt(i + 1));
      if (h == -1 || l == -1) {
        throw new IllegalArgumentException("contains illegal character for hexBinary: " + s);
      }

      out[i / 2] = (byte) (h * 16 + l);
    }

    return out;
  }

  private int hexToBin(char ch) {
    if ('0' <= ch && ch <= '9') {
      return ch - '0';
    }
    if ('A' <= ch && ch <= 'F') {
      return ch - 'A' + 10;
    }
    if ('a' <= ch && ch <= 'f') {
      return ch - 'a' + 10;
    }
    return -1;
  }

}
