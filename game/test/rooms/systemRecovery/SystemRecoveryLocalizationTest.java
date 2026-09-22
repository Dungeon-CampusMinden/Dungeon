package rooms.systemRecovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import engine.language.Language;
import engine.language.Translation;
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

  @Test
  void trackingConsentNamesTheLocalDataProcessingAndContact() {
    SystemRecovery.initLocalization();
    Game.localization().currentLanguage(Language.DE);

    String message = new Translation("systemRecovery.trackingConsent").text("local.message");

    assertTrue(message.contains("amatutat@hsbi.de"));
    assertTrue(message.contains("vollständige Terminaleingaben"));
    assertTrue(message.contains("lokal"));
    assertTrue(message.contains("Host-Computer"));
    assertTrue(message.contains("Einstellungen > Datenschutz"));
    assertFalse(message.contains("anonymisierte Spieldaten"));

    Translation consent = new Translation("systemRecovery.trackingConsent");
    assertTrue(consent.text("local.summary").contains("ohne Tracking"));
    assertEquals("Datenschutz", consent.text("local.settingsTitle"));
    assertTrue(consent.text("local.deleteMessage").contains("Savegame"));

    Game.localization().currentLanguage(Language.EN);
    assertTrue(consent.text("local.message").contains("computer hosting the game"));
    assertTrue(consent.text("local.message").contains("Settings > Privacy"));
  }

  @Test
  void centralTrackingNoticeDescribesThePlannedGermanServerDeployment() {
    SystemRecovery.initLocalization();
    Game.localization().currentLanguage(Language.DE);
    Translation consent = new Translation("systemRecovery.trackingConsent");

    String message = consent.text("central.message");
    assertTrue(message.contains("Server in Deutschland"));
    assertTrue(message.contains("90 Tage"));
    assertTrue(message.contains("vollständige Terminaleingaben"));
    assertTrue(message.contains("pseudonym"));
    assertTrue(message.contains("amatutat@hsbi.de"));

    Game.localization().currentLanguage(Language.EN);
    assertTrue(consent.text("central.message").contains("server in Germany"));
    assertTrue(consent.text("central.message").contains("90 days"));
  }
}
