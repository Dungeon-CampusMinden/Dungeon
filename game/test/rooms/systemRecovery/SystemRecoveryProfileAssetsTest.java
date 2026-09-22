package rooms.systemRecovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Checks that dialog portraits and their licenses remain packaged together. */
class SystemRecoveryProfileAssetsTest {

  @Test
  void profileImagesAndLicensesArePackagedAtDialogPaths() throws IOException {
    ClassLoader loader = getClass().getClassLoader();
    for (String name :
        List.of(
            "system_recovery_ai_profile.png",
            "system_recovery_ai_profile_pixel.png",
            "system_recovery_echo_profile_pixel.png",
            "system_recovery_search_robot_pixel.png")) {
      String path = "profiles/" + name;
      try (InputStream image = loader.getResourceAsStream(path);
          InputStream license = loader.getResourceAsStream(path + ".license.md")) {
        assertNotNull(image, path + " is missing");
        assertNotNull(license, path + " has no license file");
        String text = new String(license.readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(text.contains("- Author: amatutat"), path);
        assertTrue(text.contains("- License: CC0 1.0"), path);
      }
    }

    assertTrue(
        SystemRecoveryText.axiomCall("test")
            .contains("profiles/system_recovery_ai_profile_pixel.png"));
    assertEquals(
        "profiles/system_recovery_echo_profile_pixel.png",
        SystemRecoveryText.echoSpeakerImage());
    assertTrue(
        SystemRecoveryText.robotCall("test")
            .contains("profiles/system_recovery_search_robot_pixel.png"));
  }
}
