package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.entities.HeroFactory;
import contrib.entities.MiscFactory;
import contrib.entities.MonsterFactory;
import contrib.hud.DialogUtils;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import contrib.utils.components.item.ItemGenerator;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.System;
import core.game.ECSManagment;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import entities.BurningFireballSkill;
import item.concreteItem.ItemPotionWater;
import item.concreteItem.ItemResourceBerry;
import item.concreteItem.ItemResourceMushroomRed;
import java.io.IOException;
import java.util.logging.Level;
import level.devlevel.*;
import level.utils.DungeonLoader;
import systems.*;
import systems.DevHealthSystem;
import systems.EventScheduler;

/** Starter class for the DevDungeon game. */
public class DevDungeon {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final boolean SKIP_TUTORIAL = false;
  private static final boolean ENABLE_CHEATS = false;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
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
          TeleporterSystem teleporterSystem =
              (TeleporterSystem) Game.systems().get(TeleporterSystem.class);
          teleporterSystem.clearTeleporters();
        });

    // build and start game
    Game.run();
    Game.windowTitle("Dev Dungeon");
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.instance()
              .addLevel(
                  new Tuple("tutorial", TutorialLevel.class),
                  new Tuple("damagedBridge", DamagedBridgeRiddleLevel.class),
                  new Tuple("torchRiddle", TorchRiddleLevel.class),
                  new Tuple("illusionRiddle", IllusionRiddleLevel.class),
                  new Tuple("bridgeGuard", BridgeGuardRiddleLevel.class),
                  new Tuple("finalBoss", BossLevel.class));
          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(() -> DungeonLoader.instance().loadNextLevel());

          createSystems();
          FogOfWarSystem fogOfWarSystem = (FogOfWarSystem) Game.systems().get(FogOfWarSystem.class);
          fogOfWarSystem.active(false); // Default: Fog of War is disabled

          HeroFactory.setHeroSkillCallback(
              new BurningFireballSkill(
                  SkillTools::cursorPositionAsPoint)); // Override default skill
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          setupMusic();
          Crafting.loadRecipes();
          if (SKIP_TUTORIAL) {
            DungeonLoader.instance().loadLevel(1); // First Level
          } else {
            DungeonLoader.instance().loadLevel(0); // Tutorial
          }
        });
  }

  private static void createHero() throws IOException {
    Entity hero = HeroFactory.newHero();
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

    // Set up random item generator for chests and monsters
    ItemGenerator ig = new ItemGenerator();
    ig.addItem(() -> new ItemPotionHealth(HealthPotionType.randomType()), 1);
    ig.addItem(ItemPotionWater::new, 1);
    ig.addItem(ItemResourceBerry::new, 2);
    ig.addItem(ItemResourceMushroomRed::new, 2);

    MiscFactory.randomItemGenerator(ig);
    MonsterFactory.randomItemGenerator(ig);
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
    Game.add(new TeleporterSystem());
    Game.add(EventScheduler.getInstance());
    Game.add(new FogOfWarSystem());
    Game.add(new LeverSystem());
    Game.add(new MobSpawnerSystem());
    Game.add(new MagicShieldSystem());

    /* Cheats */
    if (ENABLE_CHEATS) {
      enableCheats();
    }
  }

  private static void enableCheats() {
    Game.add(
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
              HeroFactory.setHeroSkillCallback(
                  new BurningFireballSkill(
                      SkillTools::cursorPositionAsPoint)); // Update the current hero skill
              DialogUtils.showTextPopup(
                  "Fireball damage set to " + BurningFireballSkill.DAMAGE_AMOUNT,
                  "Cheat: Fireball Damage");
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
              Debugger.TELEPORT_TO_END();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) {
              FallingSystem.DEBUG_DONT_KILL = !FallingSystem.DEBUG_DONT_KILL;
              DialogUtils.showTextPopup(
                  "Falling damage is now "
                      + (FallingSystem.DEBUG_DONT_KILL ? "disabled" : "enabled"),
                  "Cheat: Falling Damage");
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_5)) {
              Debugger.TELEPORT_TO_CURSOR();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_6)) {
              BurningFireballSkill.UNLOCKED = !BurningFireballSkill.UNLOCKED;
            }
          }
        });
  }
}
