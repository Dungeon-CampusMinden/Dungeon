package rooms.mushroom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import engine.game.PreRunConfiguration;
import engine.level.loader.DungeonLoader;
import engine.utils.Tuple;
import engine.utils.components.draw.ColorUtils;
import engine.utils.components.draw.TextureGenerator;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.ShaderList;
import engine.utils.components.path.SimpleIPath;
import feature.components.Debugger;
import feature.entities.CharacterClass;
import feature.entities.EntityFactory;
import feature.level.visibility.LevelHideSystem;
import feature.systems.CollisionSystem;
import feature.systems.HealthSystem;
import feature.systems.IdleSoundSystem;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelTickSystem;
import feature.systems.LeverSystem;
import feature.systems.PressurePlateSystem;
import feature.systems.ProjectileSystem;
import java.io.IOException;
import rooms.mushroom.modules.items.MagicLensItem;
import rooms.mushroom.modules.journal.JournalItem;
import rooms.mushroom.modules.mushrooms.Mushrooms;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runDemoRoom}.
 */
public class MushRoom {
  private static final boolean DEBUG_MODE = true;
  private static final String BACKGROUND_MUSIC = "sounds/forest_bgm.wav";
  private static final String AMBIENT_MUSIC = "sounds/forest_ambient.wav";
  private static final int START_LEVEL = 0;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    Game.windowTitle("MushRoom");
    Game.run();
  }

  private static void onSetup() {
    PreRunConfiguration.enableCheckPattern(false);
    Game.userOnSetup(
        () -> {
          setupMusic();
          DungeonLoader.addLevel(Tuple.of("mushroom", MainLevel.class));
          createTextures();
          createSystems();
          createHero();
          DungeonLoader.loadLevel(START_LEVEL);
        });
  }

  private static void createTextures() {
    String basePath = "objects/mushroom.png";
    float baseHue = 0.0f;

    for (Mushrooms mushroomType : Mushrooms.values()) {
      ShaderList shaderList = new ShaderList();

      Color color = mushroomType.baseColor();
      Color outline = mushroomType.outlineColor();
      float[] hsv = new float[3];
      shaderList.add("hueRemap", new HueRemapShader(baseHue, color.toHsv(hsv)[0] / 360f));
      shaderList.add("outline", new OutlineShader(1, ColorUtils.withAlpha(outline, 0.8f)));

      TextureGenerator.registerRenderShaderTexture(
          basePath, mushroomType.getTexturePath(), shaderList);
    }
  }

  private static void createHero() {
    Entity hero = EntityFactory.newHero(CharacterClass.MUSHROOM_WIZARD);
    hero.fetch(InputComponent.class)
        .ifPresent(
            ic -> {
              ic.registerCallback(Input.Keys.B, JournalItem::openJournal, false, false);
              ic.registerCallback(Input.Keys.V, MagicLensItem::toggleMagicLens, false, false);
            });
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        feature.input.configuration.KeyboardConfig.class,
        engine.configuration.KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(60);
  }

  private static void createSystems() {
    Game.add(new LevelHideSystem());

    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new LevelTickSystem());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.075f);

    backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(AMBIENT_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.15f);
  }
}
