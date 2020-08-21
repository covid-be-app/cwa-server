package app.coronawarn.server.services.submission.util;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

  public static String TEXT = "TEST REQUEST";

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

}
