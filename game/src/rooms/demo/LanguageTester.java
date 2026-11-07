package rooms.demo;

import static engine.input.Binding.key;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.input.ActionId;
import engine.input.ContextLayer;
import engine.input.ContextSpec;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.language.Language;
import engine.language.Localization;
import engine.level.DungeonLevel;
import engine.level.loader.DungeonLoader;
import engine.utils.Tuple;
import engine.utils.components.draw.TextureGenerator;
import feature.entities.CharacterClass;
import feature.entities.EntityFactory;
import feature.hud.DialogUtils;

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
    addLanguageSystem();
    Game.disableAudio(true);
    Game.frameRate(60);
    Game.userOnSetup(
        () -> {
          TextureGenerator.registerGenerateColorTexture(TEST_PATH_DE, 100, 100, Color.RED);
          TextureGenerator.registerGenerateColorTexture(TEST_PATH_EN, 100, 100, Color.GREEN);
          Game.add(EntityFactory.newHero(CharacterClass.HUNTER));
        });

    Game.windowTitle("Language Test Room");
    Game.run();
  }

  private static final ActionId POPUP = new ActionId("langtest", "popup", key(Keys.M));
  private static final ActionId LANGUAGE = new ActionId("langtest", "language", key(Keys.N));
  private static final ActionId IMAGE = new ActionId("langtest", "image", key(Keys.B));

  private static void addLanguageSystem() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/escapeRoom/de.json");
    localization.registerTranslationFile(Language.EN, "language/escapeRoom/en.json");

    GameInput.activate(
        new InputContext(ContextSpec.of("langtest", ContextLayer.ROOM, POPUP, LANGUAGE, IMAGE))
            // Shows a text popup; ("text", "test", "fail") shows the fallback case in English.
            .onPress(
                POPUP,
                () ->
                    DialogUtils.showTextPopup(
                        localization.text("text.test.message"),
                        localization.text("text.test.title")))
            // Changes the language between English and German.
            .onPress(
                LANGUAGE,
                () ->
                    localization.currentLanguage(
                        localization.currentLanguage() == Language.DE ? Language.EN : Language.DE))
            // Shows an image in the current language.
            .onPress(
                IMAGE,
                () -> DialogUtils.showImagePopUp(localization.asset("images/open-book.png"))));
  }
}
