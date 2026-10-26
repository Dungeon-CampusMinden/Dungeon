package feature.audience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import org.junit.jupiter.api.Test;

/** Tests where the stepper puts heroes whose hitbox sits at different places in their sprite. */
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

  private record Rectangle(Vector2 offset, Vector2 size) {}
}
