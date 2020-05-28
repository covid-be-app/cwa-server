import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "verify-signature",
    subcommands = {VerifyCommand.class},
    synopsisSubcommandLabel = "(generate | verify)",
    version = "0.2",
    mixinStandardHelpOptions = true,
    description = "Generates and verifies example exposure keys.")
public class VerifyCli implements Runnable {

  public static void main(String... args) {
    Security.addProvider(new BouncyCastleProvider());
    new CommandLine(new VerifyCli()).execute(args);
  }

  public void run() {
    new CommandLine(this).usage(System.err);
  }
}
