package rooms.mushroom;

import static engine.input.Binding.key;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import engine.input.ActionId;
import engine.input.ContextLayer;
import engine.input.ContextSpec;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.level.loader.DungeonLoader;
import engine.utils.Tuple;
import engine.utils.components.draw.ColorUtils;
import engine.utils.components.draw.TextureGenerator;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.ShaderList;
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
    // The level has no character markers, so this is the only class the selection offers.
    PreRunConfiguration.multiplayerCharacterClasses(CharacterClass.MUSHROOM_WIZARD);
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

  private static final ActionId MAGIC_LENS = new ActionId("mushroom", "magic_lens", key(Keys.V));

  private static void createHero() {
    Entity hero = EntityFactory.newHero(CharacterClass.MUSHROOM_WIZARD);
    Game.add(hero);
    GameInput.activate(
        new InputContext(
                ContextSpec.of("mushroom", ContextLayer.ROOM, JournalItem.OPEN, MAGIC_LENS))
            .onPress(JournalItem.OPEN, () -> Game.player().ifPresent(JournalItem::openJournal))
            .onPress(MAGIC_LENS, () -> Game.player().ifPresent(MagicLensItem::toggleMagicLens)));
  }

  private static void configGame() throws IOException {
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
