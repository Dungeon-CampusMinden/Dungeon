package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.System;
import core.game.ECSManagment;
import core.level.Tile;
import core.systems.LevelSystem;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import entities.DevHeroFactory;
import entities.MonsterType;
import item.concreteItem.ItemPotionAttackSpeedPotion;
import java.io.IOException;
import java.util.logging.Level;
import level.utils.DungeonLoader;
import systems.*;
import systems.DevHealthSystem;
import systems.EventScheduler;
import utils.EntityUtils;

public class DevDungeon {

  public static final DungeonLoader DUNGEON_LOADER =
      new DungeonLoader(
          new String[] {
            "tutorial", "damagedBridge", "torchRiddle", "illusionRiddle", "bridgeGuard", "finalBoss"
          });
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final boolean SKIP_TUTORIAL = true;

  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    Debugger debugger = new Debugger();
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
        });

    onFrame(debugger);

    // build and start game
    Game.run();
    Game.windowTitle("Dev Dungeon");
  }

  private static void onFrame(Debugger debugger) {
    Game.userOnFrame(debugger::execute);
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
    // backgroundMusic.play();
    backgroundMusic.setVolume(.1f);
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
    Game.add(new LevelEditorSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(TeleporterSystem.getInstance());
    Game.add(EventScheduler.getInstance());
    Game.add(new FogOfWarSystem());
    Game.add(new LeverSystem());
    Game.add(new MobSpawnerSystem());
    Game.add(new MagicShieldSystem());
    Game.add(
        new System() {
          @Override
          public void execute() {
            Point mosPos = SkillTools.cursorPositionAsPoint();
            mosPos = new Point(mosPos.x - 0.5f, mosPos.y - 0.25f);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0)) {
              EntityUtils.spawnMonster(MonsterType.CHORT, mosPos);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
              EntityUtils.spawnMonster(MonsterType.IMP, mosPos);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
              EntityUtils.spawnMonster(MonsterType.ZOMBIE, mosPos);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
              EntityUtils.spawnSign("Hello World", "Schild", mosPos);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) {
              Game.hero()
                  .orElseThrow()
                  .fetch(InventoryComponent.class)
                  .orElseThrow()
                  .add(new ItemPotionAttackSpeedPotion());
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_5)) {
              Tile tile = LevelSystem.level().tileAt(mosPos);
              if (tile != null) {
                java.lang.System.out.println(tile);
              } else {
                java.lang.System.out.println("Tile - null");
              }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_6)) {
              FogOfWarSystem foWSystem =
                  ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class));
              foWSystem.active(!foWSystem.active());
            }
          }
        });
  }
}
