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
