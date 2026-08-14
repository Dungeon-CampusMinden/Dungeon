package rooms.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.System;
import engine.configuration.KeyboardConfig;
import engine.language.Language;
import engine.language.Localization;
import engine.level.DungeonLevel;
import engine.level.loader.DungeonLoader;
import engine.utils.Tuple;
import engine.utils.components.draw.TextureGenerator;
import engine.utils.components.path.SimpleIPath;
import feature.entities.CharacterClass;
import feature.entities.EntityFactory;
import feature.hud.DialogUtils;
import java.io.IOException;

/**
 * Starter to test the Localization class.
 *
 * <p>Use the Key "M" to open die Show-Text. Use the Key "N" to switch between the languages English
 * and German.
 */
public class LanguageTester {
  private static final String TEST_PATH_DE = "images/open-book_de.png";
  private static final String TEST_PATH_EN = "images/open-book_en.png";

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   */
  public static void main(String[] args) {
    DungeonLoader.addLevel(Tuple.of("language", DungeonLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
      addLanguageSystem();
      Game.disableAudio(true);
      Game.frameRate(60);
      Game.userOnSetup(
          () -> {
            TextureGenerator.registerGenerateColorTexture(TEST_PATH_DE, 100, 100, Color.RED);
            TextureGenerator.registerGenerateColorTexture(TEST_PATH_EN, 100, 100, Color.GREEN);
            Game.add(EntityFactory.newHero(CharacterClass.HUNTER));
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Game.windowTitle("Language Test Room");
    Game.run();
  }

  private static void addLanguageSystem() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");

    Game.add(
        new System() {
          @Override
          public void execute() {
            // Shows a text popup by pressing "M". ("text", "test", "fail") Shows the fallback case
            // in English.
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
              DialogUtils.showTextPopup(
                  localization.text("text.test.message"), localization.text("text.test.title"));
            }

            // Changes the language between English and German by pressing "N".
            if (Gdx.input.isKeyJustPressed(Input.Keys.N)
                && localization.currentLanguage() == Language.DE) {
              localization.currentLanguage(Language.EN);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.N)
                && localization.currentLanguage() == Language.EN) {
              localization.currentLanguage(Language.DE);
            }
            // Shows an image in the current language.
            if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
              String path = localization.asset("images/open-book.png");
              DialogUtils.showImagePopUp(path);
            }
          }
        });
  }
}
