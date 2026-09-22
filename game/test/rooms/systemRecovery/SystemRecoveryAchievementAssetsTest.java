package rooms.systemRecovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.utils.JsonHandler;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.SystemRecoveryAchievements;

/** Keeps the achievement definition, packaged images and per-image licenses in sync. */
class SystemRecoveryAchievementAssetsTest {

  @Test
  void everyAchievementImageAndAtlasHavePackagedLicenses() throws IOException {
    ClassLoader loader = getClass().getClassLoader();
    Map<String, Object> definition;
    try (InputStream input =
        loader.getResourceAsStream(SystemRecoveryAchievements.DEFINITION_PATH)) {
      assertNotNull(input, "System Recovery achievement definition is missing");
      definition =
          JsonHandler.readJson(new String(input.readAllBytes(), StandardCharsets.UTF_8));
    }

    List<?> achievements = assertInstanceOf(List.class, definition.get("achievements"));
    assertEquals(25, achievements.size());
    for (Object entry : achievements) {
      Map<?, ?> achievement = assertInstanceOf(Map.class, entry);
      String imagePath = assertInstanceOf(String.class, achievement.get("imagePath"));
      assertTrue(imagePath.startsWith("achievements/"), imagePath);
      assertLicensed(loader, imagePath);
    }
    assertLicensed(loader, "achievements/system_recovery_achievement_atlas.png");
  }

  private static void assertLicensed(ClassLoader loader, String imagePath) throws IOException {
    try (InputStream image = loader.getResourceAsStream(imagePath);
        InputStream license = loader.getResourceAsStream(imagePath + ".license.md")) {
      assertNotNull(image, imagePath + " is missing");
      assertNotNull(license, imagePath + " has no license file");
      String text = new String(license.readAllBytes(), StandardCharsets.UTF_8);
      assertTrue(text.contains("- Author: amatutat"), imagePath);
      assertTrue(text.contains("- License: CC0 1.0"), imagePath);
      assertTrue(text.contains("KI-"), imagePath);
    }
  }
}
