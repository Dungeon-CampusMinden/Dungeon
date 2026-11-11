package rooms.soulweaver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import engine.language.Language;
import org.junit.jupiter.api.Test;
import rooms.soulweaver.util.SoulweaverText;

/** Checks that clients translate only what the server sends as a key. */
class SoulweaverLocalizationTest {

  @Test
  void playerInputIsNeverReadAsAKey() {
    Game.localization().registerTranslationFile(Language.DE, "language/soulweaver/de.json");
    Game.localization().registerTranslationFile(Language.EN, "language/soulweaver/en.json");
    Language previous = Game.localization().currentLanguage();
    String invalid =
        SoulweaverText.key(
            "methods.error.invalid-expression",
            SoulweaverText.expression("soulweaver.story.letter"));
    String empty =
        SoulweaverText.key("methods.error.invalid-variable", SoulweaverText.expression(""));
    try {
      Game.localization().currentLanguage(Language.EN);
      assertEquals(
          "Invalid expression: soulweaver.story.letter", SoulweaverText.translate(invalid));
      assertEquals("Ungültiger Ausdruck: soulweaver.story.letter", SoulweaverText.german(invalid));
      assertEquals("Invalid variable name: ", SoulweaverText.translate(empty));
      assertTrue(
          SoulweaverText.translate(
                  SoulweaverText.key("methods.feedback.built", "soulweaver.story.letter"))
              .contains("soulweaver.story.letter"));
      // A quest-log note is translated as a whole; a key that names no text stays as typed.
      assertEquals("Ask soulweaver.unknown", SoulweaverText.translate("Ask soulweaver.unknown"));
    } finally {
      Game.localization().currentLanguage(previous);
    }
  }
}
