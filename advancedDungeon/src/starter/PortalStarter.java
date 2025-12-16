package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
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
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Consumer;
import level.portal.*;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.portalSkills.PortalSkill;
import produsAdvanced.abstraction.portals.systems.PortalExtendSystem;
import systems.AntiMaterialBarrierSystem;
import systems.LasergridSystem;

/**
 * Starter for the Demo Escaperoom Dungeon.
 *
 * <p>Usage: run with the Gradle task {@code runDemoRoom}.
 */
public class PortalStarter {
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

    Game.windowTitle("Demo-Room");
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          // setupMusic();
          DungeonLoader.addLevel(Tuple.of("PortalDemo", PortalDemoLevel.class));
          DungeonLoader.addLevel(Tuple.of("portallevel1", PortalLevel_1.class));
          DungeonLoader.addLevel(Tuple.of("portallevel2", PortalLevel_2.class));
          DungeonLoader.addLevel(Tuple.of("portallevel3", PortalLevel_3.class));
          DungeonLoader.addLevel(Tuple.of("portallevel6", PortalLevel_6.class));
          DungeonLoader.addLevel(Tuple.of("portallevel7", PortalLevel_7.class));

          createSystems();
          createHero();
          DungeonLoader.loadLevel(START_LEVEL);
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
    Game.disableAudio(false);
    Game.frameRate(30);
  }

  private static void createSystems() {
    if (DEBUG_MODE) Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new ManaBarSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new StaminaRestoreSystem());
    Game.add(new StaminaBarSystem());
    Game.add(new AISystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HealthSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    if (!DEBUG_MODE) Game.add(new FallingSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new LeverSystem());
    Game.add(new PressurePlateSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new PortalExtendSystem());
    Game.add(new AntiMaterialBarrierSystem());
    Game.add(new LasergridSystem());
    Game.add(new AttachmentSystem());
    if (DEBUG_MODE) Game.add(new Debugger());
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.05f);
  }
}
