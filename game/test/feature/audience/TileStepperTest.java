package feature.audience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import org.junit.jupiter.api.Test;

/**
 * Tests where the stepper puts heroes whose hitbox sits at different places in their sprite, and
 * which tiles an entity takes up for "Benutzen".
 */
class TileStepperTest {

  private static Point placed(Rectangle hitbox) {
    Entity hero = new Entity("hero");
    hero.add(new PositionComponent(new Point(0, 0)));
    hero.add(new CollideComponent(hitbox.offset(), hitbox.size()));
    Tile tile = mock(Tile.class);
    when(tile.position()).thenReturn(new Point(6, 1));
    new TileStepper(hero).place(tile);
    return hero.fetch(PositionComponent.class).orElseThrow().position();
  }

  private static void assertNear(Point expected, Point actual) {
    assertEquals(expected.x(), actual.x(), 1e-4f);
    assertEquals(expected.y(), actual.y(), 1e-4f);
  }

  @Test
  void heroOfOneTileStandsOnTheTileOrigin() {
    // The wizard has a hitbox of 0.8 at 0.1, centered in its one-tile sprite.
    assertNear(
        new Point(6, 1), placed(new Rectangle(Vector2.of(0.1f, 0.1f), Vector2.of(0.8f, 0.8f))));
  }

  @Test
  void largerHeroHasItsHitboxCenteredOnTheTile() {
    // The rogue's two-tile sprite has its hitbox at 0.6 and 0.5.
    assertNear(
        new Point(5.5f, 0.6f),
        placed(new Rectangle(Vector2.of(0.6f, 0.5f), Vector2.of(0.8f, 0.8f))));
  }

  private static Entity entity(Point position, Rectangle hitbox) {
    Entity entity = new Entity("object");
    entity.add(new PositionComponent(position));
    entity.add(new CollideComponent(hitbox.offset(), hitbox.size()));
    return entity;
  }

  @Test
  void wideHitboxTakesUpEveryTileItReaches() {
    // The whiteboard hangs on a wall and is two tiles wide.
    Entity board = entity(new Point(5, 11), new Rectangle(Vector2.ZERO, Vector2.of(2, 1)));
    assertTrue(TileStepper.covers(board, new Point(5, 11)));
    assertTrue(TileStepper.covers(board, new Point(6, 11)));
    assertFalse(TileStepper.covers(board, new Point(7, 11)));
    assertFalse(TileStepper.covers(board, new Point(5, 12)));
  }

  @Test
  void hitboxOfOneTileTakesUpOnlyItsTile() {
    Entity station = entity(new Point(3, 2), new Rectangle(Vector2.ZERO, Vector2.ONE));
    assertTrue(TileStepper.covers(station, new Point(3, 2)));
    assertFalse(TileStepper.covers(station, new Point(4, 2)));
    assertFalse(TileStepper.covers(station, new Point(2, 2)));
    assertFalse(TileStepper.covers(station, new Point(3, 3)));
  }

  @Test
  void entityWithoutHitboxTakesUpTheTileOfItsPosition() {
    Entity sign = new Entity("sign");
    sign.add(new PositionComponent(new Point(2.4f, 7.2f)));
    assertTrue(TileStepper.covers(sign, new Point(2, 7)));
    assertFalse(TileStepper.covers(sign, new Point(3, 7)));
  }

  private record Rectangle(Vector2 offset, Vector2 size) {}
}
