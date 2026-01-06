package starter;

import contrib.components.*;
import contrib.entities.EntityFactory;
import contrib.hud.DialogUtils;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import contrib.utils.components.skill.selfSkill.SelfHealSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.elements.ILevel;
import core.level.loader.DungeonLoader;
import core.utils.Direction;
import core.utils.JsonHandler;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import portal.level.*;
import portal.portals.PortalColor;
import portal.portals.PortalExtendSystem;
import portal.portals.PortalSkill;
import portal.antiMaterialBarrier.AntiMaterialBarrierSystem;
import portal.laserGrid.LasergridSystem;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runDemoRoom}.
 */
public class PortalStarter {
  private static final boolean DEBUG_MODE = true;
  private static final int START_LEVEL = 0;
  private static final String SAVE_LEVEL_KEY = "LEVEL";
  private static final String SAVE_FILE = "currentPortalLevel.json";

  /**
   * Main method to start the game.
   *
   * @param args The arguments passed to the game.
   * @throws IOException If an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {
    configGame();
    onSetup();
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("portallevel1", PortalLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("portallevel2", PortalLevel_2.class));
          DungeonLoader.addLevel(Tuple.of("portallevel3", PortalLevel_3.class));
          DungeonLoader.addLevel(Tuple.of("portallevel4", PortalLevel_4.class));
          DungeonLoader.addLevel(Tuple.of("portallevel5", PortalLevel_5.class));
          DungeonLoader.addLevel(Tuple.of("portallevel6", PortalLevel_6.class));
          DungeonLoader.addLevel(Tuple.of("portallevel7", PortalLevel_7.class));
          DungeonLoader.addLevel(Tuple.of("PortalDemo", PortalDemoLevel.class));
          createSystems();
          createHero();
          DungeonLoader.loadLevel(loadLevelIndex());
        });
  }

  private static void createHero() {
    Entity chell = EntityFactory.newHero(death_callback);
    chell
        .fetch(SkillComponent.class)
        .ifPresent(
            skillComponent -> {
              skillComponent.addSkill(
                  new PortalSkill(PortalColor.BLUE, new Tuple<>(Resource.MANA, 0)));
              skillComponent.addSkill(
                  new PortalSkill(PortalColor.GREEN, new Tuple<>(Resource.MANA, 0)));
              skillComponent.removeSkill(FireballSkill.class);
              skillComponent.removeSkill(SelfHealSkill.class);
            });
    Game.add(chell);
  }

  private static Consumer<Entity> death_callback =
      (hero) ->
          DialogUtils.showTextPopup(
              "You died!",
              "Game Over",
              () -> {
                // Just respawn at Start Tile instead of reloading the level
                hero.fetch(PositionComponent.class)
                    .ifPresent(
                        pc -> {
                          pc.position(Game.currentLevel().flatMap(ILevel::startTile).orElseThrow());
                          pc.viewDirection(Direction.DOWN);
                          PositionSync.syncPosition(hero);
                        });

                hero.fetch(VelocityComponent.class)
                    .ifPresent(
                        vc -> {
                          vc.clearForces();
                          vc.currentVelocity(Vector2.ZERO);
                        });

                hero.fetch(HealthComponent.class)
                    .ifPresent(
                        hc -> {
                          hc.currentHealthpoints(hc.maximalHealthpoints());
                          hc.clearDamage();
                          hc.alreadyDead(false);
                        });

                hero.fetch(ManaComponent.class).ifPresent(hc -> hc.currentAmount(hc.maxAmount()));
                hero.fetch(StaminaComponent.class)
                    .ifPresent(hc -> hc.currentAmount(hc.maxAmount()));

                // reset inventory
                hero.fetch(CharacterClassComponent.class)
                    .ifPresent(
                        characterClassComponent -> {
                          InventoryComponent invComp =
                              new InventoryComponent(
                                  characterClassComponent.characterClass().inventorySize());
                          characterClassComponent
                              .characterClass()
                              .startItems()
                              .forEach(invComp::add);
                          hero.add(invComp);
                        });

                // reset the animation queue
                hero.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);

                DungeonLoader.reloadCurrentLevel();
              });

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(30);
    Game.userOnLevelLoad(
        aBoolean -> {
          if (aBoolean) {
            writeLevelIndex(DungeonLoader.currentLevelIndex());
          }
        });
    Game.resizeable(true);
    Game.windowTitle("Portal Dungeon");
  }

  private static void createSystems() {
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    else Game.add(new Debugger());
    // Portal specific systems
    Game.add(new PortalExtendSystem());
    Game.add(new AntiMaterialBarrierSystem());
    Game.add(new LasergridSystem());
    Game.add(new AttachmentSystem());
  }

  private static int loadLevelIndex() {
    File file = new File(SAVE_FILE);

    if (!file.exists()) {
      return 0;
    }
    try {
      String json = readFileContent(file);
      Map<String, Object> map = JsonHandler.readJson(json);
      return ((Long) map.getOrDefault(SAVE_LEVEL_KEY, 0)).intValue();
    } catch (IOException e) {
      return 0;
    }
  }

  private static String readFileContent(File file) throws IOException {
    try (InputStream fis = new FileInputStream(file)) {
      return new String(fis.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IOException("Failed to read game configuration file: " + file.getPath(), e);
    }
  }

  private static void writeLevelIndex(int index) {
    HashMap<String, Object> map = new HashMap<>();
    map.put(SAVE_LEVEL_KEY, index);
    String content = JsonHandler.writeJson(map, true);
    try {
      File file = new File(SAVE_FILE);
      // Ensure parent directory exists
      try (OutputStreamWriter osw =
          new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
        osw.write(content);
      }
    } catch (Exception ignored) {
    }
  }
}
