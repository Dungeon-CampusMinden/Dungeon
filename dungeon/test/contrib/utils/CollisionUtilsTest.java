package contrib.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.systems.LevelSystem;
import core.systems.MoveSystem;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link MoveSystem}. */
public class CollisionUtilsTest {

  private static final Point START_POSITION = new Point(5, 5);
  private static final float MAX_SPEED = 10f;
  private Entity entity;
  private VelocityComponent vc;
  private PositionComponent pc;

  @BeforeEach
  void setup() {
    entity = new Entity();
    pc = new PositionComponent(START_POSITION);
    vc = new VelocityComponent(MAX_SPEED);
    entity.add(vc);
    entity.add(pc);
    Game.add(entity);
    Game.add(new LevelSystem(() -> {}));

    // Setup initial position and velocity default
    vc.currentVelocity(Vector2.ZERO);
    vc.canEnterOpenPits(false);
  }

  @AfterEach
  void cleanUp() {
    Game.removeAllSystems();
    Game.removeAllEntities();
  }

  /**
   * Tests that the isPathClearByStepping method returns true when all tiles along the path from the
   * start point to the end point are accessible.
   *
   * <p>This verifies that the method correctly detects a clear path without obstacles when stepping
   * through tiles incrementally.
   */
  @Test
  void isPathClearByStepping_returnsTrueForClearPath() {
    Point start = new Point(0, 0);
    Point end = new Point(1, 0);

    // Mock tiles along the path to be accessible
    DungeonLevel level = mock(DungeonLevel.class);
    when(level.tileAt(any(Point.class)))
        .thenAnswer(
            invocation -> {
              Point p = invocation.getArgument(0);
              Tile t = mock(Tile.class);
              when(t.isAccessible()).thenReturn(true);
              return Optional.of(t);
            });
    Game.currentLevel(level);

    boolean result = CollisionUtils.isPathClearByStepping(start, end, false, false);
    assertTrue(result, "Path should be clear when all tiles are accessible");
  }

  /**
   * Tests that the isPathClearByStepping method returns false if any tile along the path from the
   * start point to the end point is inaccessible.
   *
   * <p>This ensures the method correctly detects blocked paths by checking each intermediate tile.
   */
  @Test
  void isPathClearByStepping_returnsFalseIfTileInPathBlocked() {
    Point start = new Point(0, 0);
    Point end = new Point(1, 0);

    DungeonLevel level = mock(DungeonLevel.class);
    when(level.tileAt(any(Point.class)))
        .thenAnswer(
            invocation -> {
              Point p = invocation.getArgument(0);
              Tile t = mock(Tile.class);
              if (p.x() >= 0.5) {
                when(t.isAccessible()).thenReturn(false);
              } else {
                when(t.isAccessible()).thenReturn(true);
              }
              return Optional.of(t);
            });
    Game.currentLevel(level);

    boolean result = CollisionUtils.isPathClearByStepping(start, end, false, false);
    assertFalse(result, "Path should be blocked when any tile in path is inaccessible");
  }
}
