package rooms.systemRecovery.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/** Checks that the replacement transport crate retains its source footprint and license. */
class TransportCrateAssetTest {

  @Test
  void sciFiTransportCrateIsSixteenByThirtyTwoAndLicensed() throws IOException {
    ClassLoader loader = getClass().getClassLoader();
    String path = "objects/crate/system_recovery_sort.png";
    try (InputStream image = loader.getResourceAsStream(path);
        InputStream license = loader.getResourceAsStream(path + ".license.md")) {
      assertNotNull(image, path + " is missing");
      assertNotNull(license, path + " has no license file");
      BufferedImage texture = ImageIO.read(image);
      assertNotNull(texture, path + " could not be decoded");
      assertEquals(16, texture.getWidth());
      assertEquals(32, texture.getHeight());

      String licenseText = new String(license.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(licenseText.contains("- Author: amatutat"));
      assertTrue(licenseText.contains("- License: CC0 1.0"));
      assertTrue(licenseText.contains("AI-generated"));
    }
  }
}
