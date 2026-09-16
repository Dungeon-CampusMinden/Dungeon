package rooms.systemRecovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Game;
import engine.language.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Verifies that the room entry point registers its separated translation resources. */
class SystemRecoveryLocalizationTest {

  @AfterEach
  void restoreLanguage() {
    Game.localization().currentLanguage(Language.EN);
  }

  @Test
  void roomRegistrationLoadsTheDedicatedSystemRecoveryFile() {
    SystemRecovery.initLocalization();
    Game.localization().currentLanguage(Language.EN);

    assertEquals("LEARNING STEPS", SystemRecoveryText.text("computer.debug-petri-steps"));
  }
}
