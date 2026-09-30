package rooms.systemRecovery.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.utils.Point;
import engine.utils.components.draw.shader.EnergyFillShader;
import feature.shader.ShaderComponent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/** Verifies the shared shader-fill crate keeps the original crate footprint and is licensed. */
class SystemRecoveryEnergyCrateAssetTest {

  @Test
  void energyCrateTextureMatchesOriginalDimensionsAndHasLicense() throws IOException {
    ClassLoader loader = getClass().getClassLoader();
    assertLicensedTexture(loader, EnergyEntityFactory.ENERGY_CRATE_TEXTURE, true);
    assertLicensedTexture(loader, EnergyEntityFactory.ENERGY_CRATE_FILL_TEXTURE, false);
  }

  @Test
  void fillMaskCoversOnlyTheDarkCentralGauge() throws IOException {
    try (InputStream image =
        getClass()
            .getClassLoader()
            .getResourceAsStream(EnergyEntityFactory.ENERGY_CRATE_FILL_TEXTURE)) {
      assertNotNull(image);
      BufferedImage mask = ImageIO.read(image);
      assertNotNull(mask);
      assertEquals(0, mask.getRGB(0, 0) >>> 24);
      assertEquals(0, mask.getRGB(6, 16) >>> 24);
      assertEquals(255, mask.getRGB(7, 16) >>> 24);
      assertEquals(255, mask.getRGB(8, 16) >>> 24);
      assertEquals(0, mask.getRGB(7, 25) >>> 24);
    }
    try (InputStream image =
        getClass().getClassLoader().getResourceAsStream(EnergyEntityFactory.ENERGY_CRATE_TEXTURE)) {
      assertNotNull(image);
      BufferedImage crate = ImageIO.read(image);
      assertNotNull(crate);
      int emptyGauge = crate.getRGB(7, 16);
      int emptyGaugeBrightness =
          ((emptyGauge >>> 16) & 255) + ((emptyGauge >>> 8) & 255) + (emptyGauge & 255);
      assertTrue(emptyGaugeBrightness < 100);
    }
  }

  @Test
  void transportStoragePackagesCarryTheirWeightAsASynchronizedShaderFill() {
    Entity packageEntity = TransportEntityFactory.packageEntity(new Point(0, 0), 40);
    TransportEntityFactory.addAuthoritativePackageFill(packageEntity, 40);

    ShaderComponent component = packageEntity.fetch(ShaderComponent.class).orElseThrow();
    EnergyFillShader fill = (EnergyFillShader) component.shaders().get(0).shader();
    assertEquals(0.4f, fill.fillPercentage(), 0.0001f);
    assertEquals(EnergyEntityFactory.ENERGY_CRATE_FILL_TEXTURE, fill.texturePath());
  }

  private static void assertLicensedTexture(ClassLoader loader, String path, boolean aiGenerated)
      throws IOException {
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
      if (aiGenerated) assertTrue(licenseText.contains("AI-generated"));
    }
  }
}
