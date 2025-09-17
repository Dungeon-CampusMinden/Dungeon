package core.systems;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link MoveSystem}. */
public class MoveSystemTest {

  private static final Point START_POSITION = new Point(5, 5);
  private static final float MAX_SPEED = 10f;
  private MoveSystem system;
  private Entity entity;
  private VelocityComponent vc;
  private PositionComponent pc;

  @BeforeEach
  void setup() {
    system = new MoveSystem();
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
   * Tests that the MoveSystem only processes entities that have all required components:
   * VelocityComponent and PositionComponent.
   *
   * <p>Entities missing one or more of these components must not be included in the system's
   * filtered stream.
   */
  @Test
  void onlyWorkOnCorrectEntities() {
    // Create invalid entity
    Game.add(new Entity());
    // Collect the filtered stream
    long count = system.filteredEntityStream().count();

    // Assert that only one (the valid one) is processed
    assertEquals(1, count);
  }

  /**
   * Tests that the entity's position is correctly updated according to its velocity and the frame
   * rate. Ensures that movement occurs when the velocity is within max speed and the tile is
   * accessible. This test mocks the current dungeon level and its tiles to avoid level-loading
   * dependencies.
   */
  @Test
  void movesEntityAccordingToVelocityAndFrameRate() {
    vc.currentVelocity(Vector2.of(6, 0));
    Point startPos = pc.position();

    // Mock the current level and its tile accessibility
    DungeonLevel mockedLevel = mock(DungeonLevel.class);
    Game.currentLevel(mockedLevel);
    Tile mockedTile = mock(Tile.class);
    when(mockedLevel.tileAt(any(Point.class))).thenReturn(Optional.of(mockedTile));
    when(mockedTile.isAccessible()).thenReturn(true);

    // Execute system logic
    system.execute();

    // Expected position is moved based on velocity scaled by the frame rate
    Point expectedPos = startPos.translate(Vector2.of(6, 0).scale(1f / Game.frameRate()));
    assertEquals(expectedPos, pc.position());
  }

  /**
   * Tests that velocity exceeding maxSpeed is normalized and scaled to maxSpeed before moving. The
   * VelocityComponent itself keeps the original velocity, but the MoveSystem uses a capped version
   * to calculate the new position. This test mocks the level to ensure position updates are
   * allowed.
   */
  @Test
  void clampsVelocityToMaxSpeed() {
    // Set an oversized velocity
    vc.currentVelocity(Vector2.of(30, 40)); // length > maxSpeed (10)
    float maxSpeed = vc.maxSpeed();
    Point startPos = pc.position();

    // Mock accessible level and tile
    DungeonLevel mockedLevel = mock(DungeonLevel.class);
    Game.currentLevel(mockedLevel);
    Tile mockedTile = mock(Tile.class);
    when(mockedLevel.tileAt(any(Point.class))).thenReturn(Optional.of(mockedTile));
    when(mockedTile.isAccessible()).thenReturn(true);

    // Execute movement logic
    system.execute();

    // Calculate expected new position based on capped velocity
    Vector2 expectedVelocity = Vector2.of(30, 40).normalize().scale(maxSpeed);
    Point expectedPos = startPos.translate(expectedVelocity.scale(1f / Game.frameRate()));

    assertEquals(expectedPos, pc.position());
  }

  /**
   * Tests that if the tiles at the new hitbox position are inaccessible, the entity tries to move
   * only in X or Y direction if accessible. If only X is accessible, it moves there and calls
   * onWallHit.
   */
  @Test
  void doesNotMoveIntoInaccessibleTileAndCallsWallHit() {
    vc.currentVelocity(Vector2.of(1, 1));

    Point oldPos = pc.position();
    float frameRate = Game.frameRate();
    Vector2 scaledVelocity = vc.currentVelocity().scale(1f / frameRate);
    Point newPos = oldPos.translate(scaledVelocity);
    Point xMove = new Point(newPos.x(), oldPos.y());
    Point yMove = new Point(oldPos.x(), newPos.y());

    Vector2 offset = vc.moveboxOffset();
    Vector2 size = vc.moveboxSize();

    // Helper to get hitbox corners for a given position
    Function<Point, List<Point>> hitboxCorners =
        pos ->
            List.of(
                new Point(pos.x() + offset.x(), pos.y() + offset.y()), // top-left
                new Point(pos.x() + offset.x() + size.x(), pos.y() + offset.y()), // top-right
                new Point(pos.x() + offset.x(), pos.y() + offset.y() + size.y()), // bottom-left
                new Point(
                    pos.x() + offset.x() + size.x(),
                    pos.y() + offset.y() + size.y()) // bottom-right
                );

    // Mock Tiles
    Tile accessibleTile = mock(Tile.class);
    when(accessibleTile.isAccessible()).thenReturn(true);

    Tile blockedTile = mock(Tile.class);
    when(blockedTile.isAccessible()).thenReturn(false);

    Tile defaultTile = mock(Tile.class);
    when(defaultTile.isAccessible()).thenReturn(true);
    when(defaultTile.levelElement()).thenReturn(LevelElement.FLOOR);

    // Mock Level and inject into Game
    DungeonLevel level = mock(DungeonLevel.class);
    when(level.tileAt(any(Point.class))).thenReturn(Optional.of(defaultTile));

    // Block all corners of newPos
    for (Point corner : hitboxCorners.apply(newPos)) {
      when(level.tileAt(eq(corner))).thenReturn(Optional.of(blockedTile));
    }

    // Make all corners of xMove accessible
    for (Point corner : hitboxCorners.apply(xMove)) {
      when(level.tileAt(eq(corner))).thenReturn(Optional.of(accessibleTile));
    }

    // Make all corners of yMove blocked
    for (Point corner : hitboxCorners.apply(yMove)) {
      when(level.tileAt(eq(corner))).thenReturn(Optional.of(blockedTile));
    }

    Game.currentLevel(level);

    Consumer<Entity> onWallHit = mock(Consumer.class);
    vc.onWallHit(onWallHit);

    system.execute();

    assertEquals(xMove, pc.position());
    verify(onWallHit).accept(entity);
  }

  /**
   * Tests that the entity can move into a PIT tile if canEnterOpenPits is true, even though the
   * tile is not normally accessible.
   */
  @Test
  void allowsMovementIntoPitTileWhenAllowed() {
    vc.currentVelocity(Vector2.of(1, 0));
    vc.canEnterOpenPits(true);

    Point oldPos = pc.position();
    Point newPos = oldPos.translate(vc.currentVelocity().scale(1f / Game.frameRate()));

    // Mock PIT tile
    Tile pitTile = mock(Tile.class);
    when(pitTile.isAccessible()).thenReturn(false);
    when(pitTile.levelElement()).thenReturn(LevelElement.PIT);

    // Stub all tile lookups to return the PIT tile (safe fallback)
    DungeonLevel level = mock(DungeonLevel.class);
    when(level.tileAt(any(Point.class))).thenReturn(Optional.of(pitTile));
    Game.currentLevel(level);

    system.execute();

    // Should move into PIT tile even if not accessible
    assertEquals(newPos, pc.position());
  }

  /**
   * Tests that the entity does not move into a PIT tile if canEnterOpenPits is false. In this case,
   * the position should remain unchanged and onWallHit should be triggered.
   */
  @Test
  void blocksMovementIntoPitTileWhenNotAllowed() {
    vc.currentVelocity(Vector2.of(1, 0));
    vc.canEnterOpenPits(false);

    Point oldPos = pc.position();
    Point newPos = oldPos.translate(vc.currentVelocity().scale(1f / Game.frameRate()));

    // Mock PIT tile that is not accessible
    Tile pitTile = mock(Tile.class);
    when(pitTile.isAccessible()).thenReturn(false);
    when(pitTile.levelElement()).thenReturn(LevelElement.PIT);

    // Mock level and inject into Game
    DungeonLevel level = mock(DungeonLevel.class);
    when(level.tileAt(eq(newPos))).thenReturn(Optional.of(pitTile));
    Game.currentLevel(level);

    // Setup onWallHit consumer to verify callback
    Consumer<Entity> onWallHit = mock(Consumer.class);
    vc.onWallHit(onWallHit);

    // Execute system
    system.execute();

    // Position should not have changed
    assertEquals(oldPos, pc.position());
    // onWallHit should be triggered
    verify(onWallHit, times(1)).accept(entity);
  }

  /** Tests that when velocity is zero, the position does not change. */
  @Test
  void doesNotMoveWhenVelocityIsZero() {
    vc.currentVelocity(Vector2.of(0, 0));
    Point startPos = pc.position();

    // Setup accessible tile for movement check
    DungeonLevel mockedLevel = mock(DungeonLevel.class);
    Game.currentLevel(mockedLevel);
    Tile accessibleTile = mock(Tile.class);
    when(mockedLevel.tileAt(any(Point.class))).thenReturn(Optional.of(accessibleTile));
    when(accessibleTile.isAccessible()).thenReturn(true);

    system.execute();

    // Position should remain unchanged
    assertEquals(startPos, pc.position());
  }

  /**
   * Tests that when velocity is diagonal and its magnitude exceeds maxSpeed, the velocity is capped
   * and the position updates accordingly.
   */
  @Test
  void capsDiagonalVelocityExceedingMaxSpeed() {
    // Velocity with length greater than maxSpeed
    Vector2 diagonalVelocity = Vector2.of(10, 10); // length ~14.14 > maxSpeed (assume 10)
    vc.currentVelocity(diagonalVelocity);
    Point startPos = pc.position();

    // Setup accessible tile for movement check
    DungeonLevel mockedLevel = mock(DungeonLevel.class);
    Game.currentLevel(mockedLevel);
    Tile accessibleTile = mock(Tile.class);
    when(mockedLevel.tileAt(any(Point.class))).thenReturn(Optional.of(accessibleTile));
    when(accessibleTile.isAccessible()).thenReturn(true);

    system.execute();

    // Expected capped velocity with length = maxSpeed
    Vector2 cappedVelocity = diagonalVelocity.normalize().scale(vc.maxSpeed());
    Point expectedPos = startPos.translate(cappedVelocity.scale(1f / Game.frameRate()));

    assertEquals(expectedPos, pc.position());
  }

  /**
   * Tests that when the velocity magnitude exceeds maxSpeed, the position is updated according to
   * the velocity capped at maxSpeed.
   */
  @Test
  void velocityIsCappedAtMaxSpeed() {
    // Setup VelocityComponent with maxSpeed = 5f
    Entity entity = new Entity();
    vc = new VelocityComponent(5f);
    pc = new PositionComponent();
    vc.currentVelocity(Vector2.of(10, 10)); // magnitude ~14.14 > 5f
    pc.position(new Point(0, 0));

    entity.add(vc);
    entity.add(pc);
    Game.add(entity);

    // Mock level and tile to allow movement
    DungeonLevel level = mock(DungeonLevel.class);
    Tile accessibleTile = mock(Tile.class);
    when(accessibleTile.isAccessible()).thenReturn(true);
    Point expectedPos =
        pc.position()
            .translate(Vector2.of(10, 10).normalize().scale(5f).scale(1f / Game.frameRate()));
    when(level.tileAt(any(Point.class))).thenReturn(Optional.of(accessibleTile));
    Game.currentLevel(level);

    // Execute system
    system.execute();

    // Position should be updated according to capped velocity (normalized * maxSpeed)
    assertEquals(expectedPos, pc.position());
  }

  /**
   * Tests that when diagonal movement is blocked by an inaccessible tile, but movement along the X
   * axis alone is possible, the entity moves only along the X axis.
   *
   * <p>Also verifies that the entity’s onWallHit callback is triggered when a wall blocks diagonal
   * movement.
   */
  @Test
  void updatePosition_movesOnXAxisWhenDiagonalBlockedButXAxisAccessible() {
    vc.currentVelocity(Vector2.of(1, 1));
    Point oldPos = pc.position();
    float frameRate = Game.frameRate();
    Vector2 scaledVelocity = vc.currentVelocity().scale(1f / frameRate);
    Point newPos = oldPos.translate(scaledVelocity);
    Point xMove = new Point(newPos.x(), oldPos.y());
    Point yMove = new Point(oldPos.x(), newPos.y());

    // Mock tiles for positions
    Tile newPosTile = mock(Tile.class);
    when(newPosTile.isAccessible()).thenReturn(false);

    Tile xMoveTile = mock(Tile.class);
    when(xMoveTile.isAccessible()).thenReturn(true);

    Tile yMoveTile = mock(Tile.class);
    when(yMoveTile.isAccessible()).thenReturn(false);

    DungeonLevel level = mock(DungeonLevel.class);
    when(level.tileAt(eq(newPos))).thenReturn(Optional.of(newPosTile));
    when(level.tileAt(eq(xMove))).thenReturn(Optional.of(xMoveTile));
    when(level.tileAt(eq(yMove))).thenReturn(Optional.of(yMoveTile));
    Game.currentLevel(level);

    Consumer<Entity> onWallHit = mock(Consumer.class);
    vc.onWallHit(onWallHit);

    system.execute();

    // Position should be updated only on x axis
    assertEquals(xMove.x(), pc.position().x(), 0.04f);
    assertEquals(xMove.y(), pc.position().y());

    verify(onWallHit).accept(entity);
  }
}
