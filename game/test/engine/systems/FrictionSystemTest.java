package engine.systems;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.FlyComponent;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link engine.systems.FrictionSystem}. */
public class FrictionSystemTest {

  private FrictionSystem system;
  private Entity entity;
  private VelocityComponent vc;
  private PositionComponent pc;

  @BeforeEach
  void setup() {
    system = new FrictionSystem();
    Game.add(new LevelSystem());

    entity = new Entity();
    vc = spy(new VelocityComponent(10f));
    pc = new PositionComponent();

    entity.add(vc);
    entity.add(pc);

    Game.removeAllEntities();
    Game.add(entity);

    pc.position(new Point(5, 5));
    vc.currentVelocity(Vector2.of(10, 0));
  }

  /**
   * Tests that the friction force is correctly calculated and applied based on the friction
   * coefficient of the tile underneath the entity.
   *
   * <p>The friction force should be the inverse scaled velocity multiplied by the tile's friction
   * value.
   */
  @Test
  void appliesFrictionForceBasedOnTileFriction() {
    DungeonLevel mockedLevel = mock(DungeonLevel.class);
    Game.currentLevel(mockedLevel);

    Tile tile = mock(Tile.class);
    when(tile.friction()).thenReturn(0.2f);
    when(mockedLevel.tileAt(any(Point.class))).thenReturn(Optional.of(tile));

    system.execute();

    Vector2 expectedForce = Vector2.of(10, 0).scale(0.2f).inverse();
    verify(vc).applyForce("Friction", expectedForce);
  }

  /**
   * Tests that when the entity's velocity is zero, the friction force applied is also zero.
   *
   * <p>This ensures no unnecessary force is applied when the entity is stationary.
   */
  @Test
  void applyForceIsZeroWhenVelocityIsZero() {
    vc.currentVelocity(Vector2.of(0, 0));

    DungeonLevel mockedLevel = mock(DungeonLevel.class);
    Game.currentLevel(mockedLevel);

    Tile tile = mock(Tile.class);
    when(tile.friction()).thenReturn(0.5f);
    when(mockedLevel.tileAt(any(Point.class))).thenReturn(Optional.of(tile));

    system.execute();
    verify(vc).applyForce("Friction", Vector2.ZERO);
  }

  /** Tests that the friction force is not applied on entities with the {@link FlyComponent}. */
  @Test
  void appliesNoFrictionOnFlying() {
    DungeonLevel mockedLevel = mock(DungeonLevel.class);
    Game.currentLevel(mockedLevel);
    entity.add(new FlyComponent());
    Tile tile = mock(Tile.class);
    when(tile.friction()).thenReturn(0.2f);
    when(mockedLevel.tileAt(any(Point.class))).thenReturn(Optional.of(tile));
    system.execute();
    assertFalse(vc.force("Friction").isPresent());
  }
}
