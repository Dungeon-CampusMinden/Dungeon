package starter;

import contrib.components.SkillComponent;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.selfSkill.MeleeAttackSkill;
import core.Entity;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * hero, and starts the game loop. It is mainly used to verify that the engine runs correctly with a
 * simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runBasicStarter}.
 */
public class BasicStarter {

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  public static void main(String[] args) {
    Game.initBaseLogger(Level.WARNING);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(true);
    Game.userOnSetup(
        () -> {
          try {
            Game.add(new EventScheduler());
            Game.add(new Debugger());
            Game.add(new HealthSystem());
            Game.add(new CollisionSystem());
            Game.add(HeroFactory.newHero());
            skillTestSetup(Game.hero().get());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
    Game.run();
  }

  private static void skillTestSetup(Entity hero) {
    SkillComponent skillComponent = hero.fetch(SkillComponent.class).get();
    skillComponent.removeAll();
    skillComponent.addSkill(new MeleeAttackSkill(1, DamageType.PHYSICAL,500,Vector2.ZERO, Vector2.ONE));
  }
}
