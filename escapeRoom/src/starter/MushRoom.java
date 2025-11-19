package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import contrib.entities.CharacterClass;
import contrib.entities.EntityFactory;
import contrib.modules.levelHide.LevelHideSystem;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.loader.DungeonLoader;
import core.systems.DrawSystem;
import core.utils.Tuple;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.shader.HueRemapShader;
import core.utils.components.draw.shader.OutlineShader;
import core.utils.components.draw.shader.ShaderList;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import mushRoom.MainLevel;
import mushRoom.mushroomModule.Mushrooms;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runDemoRoom}.
 */
public class MushRoom {
  private static final boolean DEBUG_MODE = true;
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
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
    Texture baseShroom = TextureMap.instance().textureAt(new SimpleIPath("objects/mushroom.png"));
    TextureRegion region = new TextureRegion(baseShroom);
    float baseHue = 0.0f;

    for (Mushrooms mushroomType : Mushrooms.values()) {
      Color color = mushroomType.getColor();
      float[] hsv = new float[3];

      ShaderList shaderList = new ShaderList();
      shaderList.add("hueRemap", new HueRemapShader(baseHue, color.toHsv(hsv)[0] / 360f));
      shaderList.add("outline", new OutlineShader(1, new Color(0, 0, 0, 0.2f)));
      FrameBuffer fbo = DrawSystem.getInstance().processShaders(region, shaderList);
      fbo.begin();
      Pixmap pm = Pixmap.createFromFrameBuffer(
        0, 0,
        fbo.getWidth(),
        fbo.getHeight()
      );
      fbo.end();

      TextureMap.instance()
        .putPixmap(
          new SimpleIPath(mushroomType.getTexturePath()), pm, true);
    }
  }

  private static void createHero() {
    Entity hero = EntityFactory.newHero(CharacterClass.MUSHROOM_WIZARD);
    Game.add(hero);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(60);
  }

  private static void createSystems() {
    Game.add(new LevelHideSystem());

    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HealthSystem());
    Game.add(new HudSystem());
    Game.add(new LevelTickSystem());
    Game.add(new EventScheduler());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }
}
