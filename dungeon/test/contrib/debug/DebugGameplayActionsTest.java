package contrib.debug;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import contrib.components.AIComponent;
import contrib.debug.controls.DebugGameplayActions;
import contrib.systems.AISystem;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.game.SystemProfile;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for backend-neutral debug gameplay actions. */
public class DebugGameplayActionsTest {

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

    DebugGameplayActions.spawnAt(spawn);

    Entity monster =
        Game.levelEntities()
            .filter(entity -> entity.isPresent(AIComponent.class))
            .findFirst()
            .orElseThrow();

    Point before =
        monster.fetch(PositionComponent.class).map(PositionComponent::position).orElseThrow();

    Point after = before;
    for (int i = 0; i < 600; i++) {
      ECSManagement.executeOneTick(core.System.AuthoritativeSide.BOTH, 1f / 60f, false);
      after = monster.fetch(PositionComponent.class).map(PositionComponent::position).orElseThrow();
      if (!before.equals(after)) {
        break;
      }
    }

    assertNotEquals(before, after);
  }

  /** Null positions are ignored instead of using exceptions as control flow. */
  @Test
  public void nullPositionDoesNotSpawnMonster() {
    assertDoesNotThrow(() -> DebugGameplayActions.spawnAt(null));

    assertFalse(Game.levelEntities().anyMatch(entity -> entity.isPresent(AIComponent.class)));
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
