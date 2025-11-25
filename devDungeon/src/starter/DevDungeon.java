package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import contrib.components.InventoryComponent;
import contrib.components.SkillComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.entities.MiscFactory;
import contrib.hud.DialogUtils;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import contrib.utils.components.item.ItemGenerator;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.BurningFireballSkill;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.System;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import item.concreteItem.ItemPotionWater;
import item.concreteItem.ItemResourceBerry;
import item.concreteItem.ItemResourceMushroomRed;
import java.io.IOException;
import level.devlevel.*;
import systems.*;

/**
 * Starter class for the DevDungeon game.
 *
 * <p>Usage: run with the Gradle task {@code runDevDungeon}.
 */
public class DevDungeon {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final boolean ENABLE_CHEATS = false;
  private static final int START_LEVEL = ENABLE_CHEATS ? 5 : 0;

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();

    Game.userOnLevelLoad(
        (firstTime) -> {
          // Resets FogOfWar on level change (prevent artifacts)
          FogOfWarSystem fogOfWarSystem = (FogOfWarSystem) Game.systems().get(FogOfWarSystem.class);
          fogOfWarSystem.reset();
          EventScheduler.clear(); // Clear all scheduled actions
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
          DungeonLoader.addLevel(
              Tuple.of("tutorial", TutorialLevel.class),
              Tuple.of("damagedBridge", DamagedBridgeRiddleLevel.class),
              Tuple.of("torchRiddle", TorchRiddleLevel.class),
              Tuple.of("illusionRiddle", IllusionRiddleLevel.class),
              Tuple.of("bridgeGuard", BridgeGuardRiddleLevel.class),
              Tuple.of("finalBoss", BossLevel.class));
          createSystems();
          FogOfWarSystem fogOfWarSystem = (FogOfWarSystem) Game.systems().get(FogOfWarSystem.class);
          fogOfWarSystem.active(false); // Default: Fog of War is disabled
          createHero();
          setupMusic();
          Crafting.loadRecipes();
          DungeonLoader.loadLevel(START_LEVEL); // Tutorial
        });
  }

  private static void createHero() {
    Entity hero = EntityFactory.newHero();
    hero.fetch(SkillComponent.class)
        .ifPresent(
            sc -> {
              sc.removeAll();
              sc.addSkill(new FireballSkill(SkillTools::cursorPositionAsPoint));
            });
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
    MiscFactory.randomItemGenerator(ig);
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
    Game.add(new FogOfWarSystem());
    Game.add(new LeverSystem());
    Game.add(new MobSpawnerSystem());
    Game.add(new MagicShieldSystem());

    /* Cheats */
    if (ENABLE_CHEATS) {
      Game.add(new Debugger());
      enableCheats();
    }
  }

  private static void enableCheats() {
    Game.add(
        new System() {
          @Override
          public void execute() {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
              Game.player()
                  .orElseThrow()
                  .fetch(InventoryComponent.class)
                  .orElseThrow()
                  .add(new ItemPotionHealth(HealthPotionType.GREATER));
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
              DamageProjectileSkill skill =
                  (DamageProjectileSkill)
                      Game.player()
                          .orElseThrow()
                          .fetch(SkillComponent.class)
                          .orElseThrow()
                          .activeSkill()
                          .orElseThrow();

              if (skill.damageAmount() <= 2) {
                skill.damageAmount(6);
              } else {
                skill.damageAmount(2);
              }
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
              Game.player()
                  .orElseThrow()
                  .fetch(SkillComponent.class)
                  .orElseThrow()
                  .addSkill(new BurningFireballSkill(SkillTools::cursorPositionAsPoint));
            }
          }
        });
  }
}
