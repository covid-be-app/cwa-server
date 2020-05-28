import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignature;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SuppressWarnings("unused")
@Command(name = "verify",
    description = "Verify export file signature integrity",
    version = "0.1",
    mixinStandardHelpOptions = true)
class VerifyCommand implements Runnable {

  @Option(names = {"--bin"},
      description = "export.bin",
      required = true)
  private File exportFile;

  @Option(names = {"--sig"},
      description = "export.sig",
      required = true)
  private File signatureFile;

  @Option(names = {"--crt"},
      description = "certificate.crt",
      required = true)
  private File certificateFile;

  @Override
  public void run() {
    try {
      byte[] certificateBytes = Files.readAllBytes(certificateFile.toPath());
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      InputStream certificateByteStream = new ByteArrayInputStream(certificateBytes);
      Certificate certificate = certificateFactory.generateCertificate(certificateByteStream);

      byte[] exportFileBytes = Files.readAllBytes(exportFile.toPath());
      byte[] signatureFileBytes = Files.readAllBytes(signatureFile.toPath());

      TEKSignatureList tekSignatureList = TEKSignatureList.parseFrom(signatureFileBytes);
      TEKSignature tekSignature = tekSignatureList.getSignatures(0);
      byte[] signatureBytes = tekSignature.getSignature().toByteArray();

      Signature payloadSignature = Signature.getInstance("SHA256withECDSA", "BC");
      payloadSignature.initVerify(certificate);
      payloadSignature.update(exportFileBytes);

      if (payloadSignature.verify(signatureBytes)) {
        System.out.println("Signature is valid! =)");
      } else {
        System.out.println("Signature is not valid! =(");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}