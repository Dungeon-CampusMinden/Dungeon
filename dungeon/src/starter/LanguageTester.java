package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.hud.DialogUtils;
import core.Game;
import core.System;
import core.configuration.KeyboardConfig;
import core.language.Language;
import core.language.Localization;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * Starter to test the Localization class.
 *
 * <p>Use the Key "M" to open die Show-Text. Use the Key "N" to switch between the languages English
 * and German.
 */
public class LanguageTester {

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
      Game.frameRate(30);
      Game.userOnSetup(() -> Game.add(EntityFactory.newHero(CharacterClass.HUNTER)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Game.windowTitle("Language Test Room");
    Game.run();
  }

  private static void addLanguageSystem() {
    Game.add(
        new System() {
          @Override
          public void execute() {
            // Shows a text popup by pressing "M". ("text", "test", "fail") Shows the fallback case
            // in English.
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
              try {
                DialogUtils.showTextPopup(
                    Localization.getInstance().text("text", "test", "message"),
                    Localization.getInstance().text("text", "test", "title"));
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }

            // Changes the language between English and German by pressing "N".
            if (Gdx.input.isKeyJustPressed(Input.Keys.N)
                && Localization.currentLanguage() == Language.DE) {
              Localization.currentLanguage(Language.EN);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.N)
                && Localization.currentLanguage() == Language.EN) {
              Localization.currentLanguage(Language.DE);
            }
            // Shows an image in the current language.
            if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
              String path = Localization.getInstance().asset("dungeon/assets/images/open-book.png");
              DialogUtils.showImagePopUp(path);
            }
          }
        });
  }
}
