package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.entities.HeroBuilder;
import contrib.hud.newhud.*;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.HudLevel;
import core.level.loader.DungeonLoader;
import core.systems.VelocitySystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/** Run this class to start the Level of the Dungeon. */
public class HudStarter {

  public static Entity hero;

  // private static HeadUpDisplay hud;
  // private static HealthBar healthBar;

  /**
   * Setup and run the game.
   *
   * @param args The command line arguments.
   * @throws IOException If an error occurs while loading.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("hudground", HudLevel.class));
          createSystems();
          createHero();

          DungeonLoader.loadLevel(0);
          creatHUD();
        });
  }

  private static void createHero() {
    Entity heroEntity = HeroBuilder.builder().build();
    Game.add(heroEntity);
    hero = heroEntity;
  }

  public static void restart() {
    Game.removeAllEntities();
    createHero();
    DungeonLoader.reloadCurrentLevel();
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new LeverSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new VelocitySystem());
    Game.add(new ManaBarSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new DebugDrawSystem());
  }

  private static void configGame() throws IOException {
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // Game.disableAudio(true);
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
  }

  private static void creatHUD() {

    Skin skin = new Skin(Gdx.files.absolute("dungeon/assets/skin/uiskin.json"));
    HeadUpDisplay hud = new HeadUpDisplay();

    HealthBar healthBar = new HealthBar(skin);
    hud.addElement(healthBar);

    ManaBar manaBar = new ManaBar(skin);
    hud.addElement(manaBar);

    StaminaBar staminaBar = new StaminaBar(skin);
    hud.addElement(staminaBar);

    AbilityBar abilityBar = new AbilityBar(skin);
    hud.addElement(abilityBar);

    Game.stage().ifPresent(stage -> stage.addActor(hud.getStage().getRoot()));
  }
}
