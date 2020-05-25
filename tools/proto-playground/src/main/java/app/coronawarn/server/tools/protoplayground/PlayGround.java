package app.coronawarn.server.tools.protoplayground;

import app.coronawarn.server.tools.protoplayground.gen.File;
import app.coronawarn.server.tools.protoplayground.gen.SubmissionPayload;
import com.google.protobuf.GeneratedMessageV3;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

public class PlayGround {

  public static void main(String[] args) throws IOException {
    var loadedFile = readFile();
    var submissionFile = readFileSubmission();

    loadedFile.writeTo(new FileOutputStream("result.bin"));
    submissionFile.writeTo(new FileOutputStream("result_submission.bin"));
  }

  private static File readFile() {
    String path = "data.yaml";
    Yaml yaml = new Yaml(new YamlConstructorForProtoBuf());
    yaml.setBeanAccess(BeanAccess.FIELD); /* no setters on RiskScoreParameters available */

    InputStream inputStream = PlayGround.class.getClassLoader().getResourceAsStream(path);

    var loaded = yaml.loadAs(inputStream, File.newBuilder().getClass());
    if (loaded == null) {
      throw new RuntimeException("Unable to load - null");
    }

    return loaded.build();
  }

  private static GeneratedMessageV3 readFileSubmission() {
    String path = "submission.yaml";
    Yaml yaml = new Yaml(new YamlConstructorForProtoBuf());
    yaml.setBeanAccess(BeanAccess.FIELD); /* no setters on RiskScoreParameters available */

    InputStream inputStream = PlayGround.class.getClassLoader().getResourceAsStream(path);

    var loaded = yaml.loadAs(inputStream, SubmissionPayload.newBuilder().getClass());
    if (loaded == null) {
      throw new RuntimeException("Unable to load - null");
    }

    return loaded.build();
  }

}
