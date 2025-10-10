package produsAdvanced;

import contrib.components.SkillComponent;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.loader.DungeonLoader;
import core.systems.VelocitySystem;
import core.utils.Tuple;
import java.io.IOException;
import java.util.logging.Level;
import produsAdvanced.abstraction.portals.portalSkills.BluePortalSkill;
import produsAdvanced.abstraction.portals.portalSkills.GreenPortalSkill;
import produsAdvanced.abstraction.portals.systems.PortalExtendSystem;
import produsAdvanced.level.TestingProdusLevel;
import systems.AntiMaterialBarrierSystem;
import systems.LasergridSystem;

/** Run this class to start the TestingGroundsLevel of the Dungeon. */
public class TestingProdusStarter {

  /**
   * Setup and run the game.
   *
   * @param args The command line arguments.
   * @throws IOException If an error occurs while loading.
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.ALL);
    configGame();
    onSetup();
    Game.run();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          DungeonLoader.addLevel(Tuple.of("testingprodus", TestingProdusLevel.class));
          createSystems();

          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          DungeonLoader.loadLevel(0);
        });
  }

  private static void createHero() throws IOException {
    Entity hero = HeroFactory.newHero();
    hero.fetch(SkillComponent.class)
        .ifPresent(
            skill -> {
              skill.addSkill(new BluePortalSkill());
              skill.addSkill(new GreenPortalSkill());
            });
    Game.add(hero);
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
    Game.add(new PressurePlateSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new VelocitySystem());
    Game.add(new ManaBarSystem());
    Game.add(new ManaRestoreSystem());
    Game.add(new Debugger());
    Game.add(new LasergridSystem());
    Game.add(new AntiMaterialBarrierSystem());
    Game.add(new PortalExtendSystem());
  }

  private static void configGame() throws IOException {
    // Game.disableAudio(true);
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
  }
}
