package contrib.debug;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import contrib.components.AIComponent;
import contrib.systems.AISystem;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.game.ECSManagement;
import core.game.SystemProfile;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for debug monster spawning. */
public class DebugMonsterSpawnerTest {

  /** Sets up a small open level and the systems needed for movement. */
  @BeforeEach
  public void setup() {
    Game.removeAllEntities();
    Game.removeAllSystems();

    ECSManagement.initializeDefaultSystems(SystemProfile.SERVER);
    ECSManagement.initializeGameplaySystems(SystemProfile.SERVER);
    Game.add(new AISystem());

    Game.currentLevel(
        new DungeonLevel(
            new LevelElement[][] {
              row(),
              row(),
              row(),
              row(),
              row(),
            },
            DesignLabel.DEFAULT));
  }

  /** Removes test entities, systems, and the active level. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** Spawns a debug monster and verifies that its idle path logic moves it. */
  @Test
  public void spawnedMonsterMoves() {
    Point spawn = new Point(2.2f, 2.2f);

    DebugMonsterSpawner.spawnAt(spawn);

    Entity monster =
        Game.levelEntities()
            .filter(entity -> entity.isPresent(AIComponent.class))
            .findFirst()
            .orElseThrow();

    Point before =
        monster.fetch(PositionComponent.class).map(PositionComponent::position).orElseThrow();

    Game.system(AISystem.class, AISystem::execute);
    assertFalse(
        monster
            .fetch(VelocityComponent.class)
            .map(vc -> vc.appliedForces().isEmpty())
            .orElse(true));

    for (int i = 0; i < 30; i++) {
      ECSManagement.executeOneTick(core.System.AuthoritativeSide.BOTH, 1f / 60f, false);
    }

    Point after =
        monster.fetch(PositionComponent.class).map(PositionComponent::position).orElseThrow();

    assertNotEquals(before, after);
  }

  /**
   * Builds one open level row.
   *
   * @return an all-floor row
   */
  private static LevelElement[] row() {
    return new LevelElement[] {
      LevelElement.FLOOR,
      LevelElement.FLOOR,
      LevelElement.FLOOR,
      LevelElement.FLOOR,
      LevelElement.FLOOR,
    };
  }
}
