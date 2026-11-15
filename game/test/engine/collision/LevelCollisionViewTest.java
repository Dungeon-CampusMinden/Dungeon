package engine.collision;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.components.CollideComponent;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.level.loader.parsers.V2FormatParser;
import engine.level.utils.DesignLabel;
import engine.resources.MutableRes;
import engine.resources.Res;
import engine.utils.Point;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LevelCollisionView}.
 *
 * <p>The tests use small real levels in a single row: {@code F} is floor, {@code W} is wall and
 * {@code H} is a hole. Tile {@code x} covers the points from {@code x} to {@code x + 1}.
 */
public class LevelCollisionViewTest {

  private static final Point FLOOR = new Point(0.5f, 0.5f);
  private static final Point WALL = new Point(1.5f, 0.5f);
  private static final Point HOLE = new Point(2.5f, 0.5f);
  private static final Point OUTSIDE = new Point(5.5f, 0.5f);

  private static ILevel level(final String row) {
    return new DungeonLevel(V2FormatParser.loadLevelLayout(List.of(row)), DesignLabel.DEFAULT);
  }

  /** A floor tile is not blocked. */
  @Test
  public void floorIsNotBlocked() {
    LevelCollisionView levelCollisionView = new LevelCollisionView(Res.of(level("FWH")));

    assertFalse(levelCollisionView.isBlocked(FLOOR, false));
  }

  /** A wall is blocked, unless the entity may enter walls. */
  @Test
  public void wallIsBlockedUnlessEntityCanEnterWalls() {
    LevelCollisionView levelCollisionView = new LevelCollisionView(Res.of(level("FWH")));

    assertTrue(levelCollisionView.isBlocked(WALL, false));
    assertFalse(levelCollisionView.isBlocked(WALL, true));
  }

  /** A hole is blocked, even for entities that may enter walls. */
  @Test
  public void holeIsAlwaysBlocked() {
    LevelCollisionView levelCollisionView = new LevelCollisionView(Res.of(level("FWH")));

    assertTrue(levelCollisionView.isBlocked(HOLE, false));
    assertTrue(levelCollisionView.isBlocked(HOLE, true));
  }

  /** A position outside the level is blocked. */
  @Test
  public void outsideOfLevelIsBlocked() {
    LevelCollisionView levelCollisionView = new LevelCollisionView(Res.of(level("FWH")));

    assertTrue(levelCollisionView.isBlocked(OUTSIDE, true));
  }

  /** Without a level, every position is blocked. */
  @Test
  public void everythingIsBlockedWithoutLevel() {
    LevelCollisionView levelCollisionView = new LevelCollisionView(Res.empty());

    assertTrue(levelCollisionView.isBlocked(FLOOR, true));
  }

  /** A collider that lies completely on floor is not blocked. */
  @Test
  public void colliderOnFloorIsNotBlocked() {
    LevelCollisionView levelCollisionView = new LevelCollisionView(Res.of(level("FWH")));
    Collider collider = new CollideComponent().collider();

    assertFalse(levelCollisionView.isBlocked(collider, new Point(0, 0), false));
  }

  /** A collider is blocked as soon as one of its corners reaches into a wall. */
  @Test
  public void colliderReachingIntoWallIsBlocked() {
    LevelCollisionView levelCollisionView = new LevelCollisionView(Res.of(level("FWH")));
    Collider collider = new CollideComponent().collider();
    Point halfIntoWall = new Point(0.5f, 0);

    assertTrue(levelCollisionView.isBlocked(collider, halfIntoWall, false));
    assertFalse(levelCollisionView.isBlocked(collider, halfIntoWall, true));
  }

  /** After a level change, the checks use the new level. */
  @Test
  public void checksFollowLevelChange() {
    MutableRes<ILevel> handle = new MutableRes<>(level("F"));
    LevelCollisionView levelCollisionView = new LevelCollisionView(handle);
    assertFalse(levelCollisionView.isBlocked(FLOOR, false));

    handle.set(level("W"));

    assertTrue(levelCollisionView.isBlocked(FLOOR, false));
  }
}
