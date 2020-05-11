package app.coronawarn.server.tools.protoplayground;

import app.coronawarn.server.tools.protoplayground.gen.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

public class PlayGround {

  public static void main(String[] args) throws IOException {
    var loadedFile = readFile();

    loadedFile.writeTo(new FileOutputStream("result.bin"));
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

}
