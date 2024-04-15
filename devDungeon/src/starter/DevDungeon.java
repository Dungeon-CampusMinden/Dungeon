package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.game.ECSManagment;
import core.systems.LevelSystem;
import core.utils.components.path.SimpleIPath;
import entities.DevHeroFactory;
import java.io.IOException;
import java.util.logging.Level;
import level.utils.DungeonLoader;
import systems.*;
import systems.DevHealthSystem;
import systems.EventScheduler;

public class DevDungeon {

  public static final DungeonLoader DUNGEON_LOADER =
      new DungeonLoader(
          new String[] {
            "tutorial", "damagedBridge", "torchRiddle", "illusionRiddle", "bridgeGuard", "finalBoss"
          });
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final boolean SKIP_TUTORIAL = false;

  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    configGame();
    onSetup();

    Game.userOnLevelLoad(
        (firstTime) -> {
          // Resets FogOfWar on level change (prevent artifacts)
          FogOfWarSystem fogOfWarSystem = (FogOfWarSystem) Game.systems().get(FogOfWarSystem.class);
          fogOfWarSystem.reset();
          EventScheduler.getInstance().clear(); // Clear all scheduled actions
          // Reset all levers
          LeverSystem leverSystem = (LeverSystem) Game.systems().get(LeverSystem.class);
          leverSystem.clear();
          // Remove all teleporters
          TeleporterSystem.getInstance().clearTeleporters();
        });

    // build and start game
    Game.run();
    Game.windowTitle("Dev Dungeon");
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(DUNGEON_LOADER::loadNextLevel);

          createSystems();
          FogOfWarSystem fogOfWarSystem = (FogOfWarSystem) Game.systems().get(FogOfWarSystem.class);
          fogOfWarSystem.active(false); // Default: Fog of War is disabled
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          setupMusic();
          Crafting.loadRecipes();
          if (SKIP_TUTORIAL) {
            DUNGEON_LOADER.loadLevel(DUNGEON_LOADER.levelOrder()[1]); // First Level
          } else {
            DUNGEON_LOADER.loadLevel(DUNGEON_LOADER.levelOrder()[0]); // Tutorial
          }
        });
  }

  private static void createHero() throws IOException {
    Entity hero = DevHeroFactory.newHero();
    Game.add(hero);
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(false);
    Game.windowTitle("DevDungeon");
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new ReviveSystem());
    Game.add(new DevHealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(TeleporterSystem.getInstance());
    Game.add(EventScheduler.getInstance());
    Game.add(new FogOfWarSystem());
    Game.add(new LeverSystem());
    Game.add(new MobSpawnerSystem());
    Game.add(new MagicShieldSystem());

    /*Game.add(
    new System() {
      @Override
      public void execute() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
          Game.hero()
              .orElseThrow()
              .fetch(InventoryComponent.class)
              .orElseThrow()
              .add(new ItemPotionHealth(HealthPotionType.GREATER));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
          if (BurningFireballSkill.DAMAGE_AMOUNT == 2) {
            BurningFireballSkill.DAMAGE_AMOUNT = 6;
          } else {
            BurningFireballSkill.DAMAGE_AMOUNT = 2;
          }
          DevHeroFactory.updateSkill();
          DialogFactory.showTextPopup(
              "Fireball damage set to " + BurningFireballSkill.DAMAGE_AMOUNT,
              "Cheat: Fireball Damage");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
          Debugger.TELEPORT_TO_END();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) {
          FallingSystem.DEBUG_DONT_KILL = !FallingSystem.DEBUG_DONT_KILL;
          DialogFactory.showTextPopup(
              "Falling damage is now "
                  + (FallingSystem.DEBUG_DONT_KILL ? "disabled" : "enabled"),
              "Cheat: Falling Damage");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_5)) {
          Debugger.TELEPORT_TO_CURSOR();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_6)) {
          BurningFireballSkill.UNLOCKED = !BurningFireballSkill.UNLOCKED;
        }
      }
    });*/
  }
}
