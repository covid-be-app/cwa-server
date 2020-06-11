package app.coronawarn.server.tools.protoplayground;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKeyExport;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class KeyFinder {

  public static void main(String[] args) throws IOException {
    var file = getFile();

    checkFile(file);
  }

  public static void checkFile(File f) throws IOException {
    var content = getExportBinContent(f);
    var export = TemporaryExposureKeyExport.parseFrom(content);

    export.getKeysList().stream().forEach(key -> {
      var keyId = Base64.getEncoder().encodeToString(key.getKeyData().toByteArray());
      System.out.println(keyId + " " + key.getRollingStartIntervalNumber() + " " + key.getTransmissionRiskLevel());
    });
  }

  private static File getFile() {
    JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

    if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      return jfc.getSelectedFile();
    }

    return null;
  }

  private static byte[] getExportBinContent(File zipFile) throws IOException {
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry zipEntry = zis.getNextEntry();
    while (zipEntry != null) {
      if (zipEntry.getName().equals("export.bin")) {
        return withoutHeader(zis.readAllBytes());
      }

      zipEntry = zis.getNextEntry();
    }

    zis.closeEntry();
    zis.close();

    throw new IllegalArgumentException("Export.bin not fiound.");
  }

  private static byte[] withoutHeader(byte[] fullContent) {
    return Arrays.copyOfRange(fullContent, 16, fullContent.length);
  }
}
