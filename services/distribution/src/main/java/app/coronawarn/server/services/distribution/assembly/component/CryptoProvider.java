

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.security.PrivateKey;
import java.security.Security;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Component;

/**
 * Wrapper component for a {@link CryptoProvider#getPrivateKey() private key} from the application properties.
 */
@Component
public class CryptoProvider {

  private final PrivateKey privateKey;

  /**
   * Creates a CryptoProvider, using {@link BouncyCastleProvider}.
   */
  CryptoProvider(DistributionServiceConfig distributionServiceConfig) {
    privateKey = loadPrivateKey(distributionServiceConfig);
    Security.addProvider(new BouncyCastleProvider());
  }

  private static PrivateKey getPrivateKeyFromString(String pemAsString) throws IOException {
    ByteArrayInputStream pemInputStream = new ByteArrayInputStream(pemAsString.getBytes());
    Reader reader = new BufferedReader(new InputStreamReader(pemInputStream));

    Object parsed = new PEMParser(reader).readObject();
    return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) parsed);
  }

  /**
   * Returns the {@link PrivateKey} configured in the application properties.
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  private PrivateKey loadPrivateKey(DistributionServiceConfig distributionServiceConfig) {
    try {
      String pemAsString = distributionServiceConfig.getPrivateKeyContent();
      return getPrivateKeyFromString(pemAsString);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load private key", e);
    }
  }
}
