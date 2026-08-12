package engine.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.elements.tile.FloorTile;
import engine.level.utils.Coordinate;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link PositionSystem} class. */
public class PositionSystemTest {

  private final Tile mock = Mockito.mock(FloorTile.class);
  private final Point point = new Point(0, 0);
  private final ILevel level = Mockito.mock(ILevel.class);
  private PositionSystem system;
  private Entity entity;
  private PositionComponent pc;

  /** Setup before each test. */
  @BeforeEach
  public void setup() {
    pc = new PositionComponent();
    Game.add(new LevelSystem());
    Game.currentLevel(level);
    system = new PositionSystem();
    Game.add(system);
    entity = new Entity();
    Game.add(entity);

    entity.add(pc);

    Mockito.when(level.randomTile(LevelElement.FLOOR)).thenReturn(Optional.of(mock));
    Mockito.when(mock.position()).thenReturn(point);
    Mockito.when(mock.coordinate()).thenReturn(new Coordinate(3, 3));
  }

  /** Cleanup after each test. */
  @AfterEach
  public void cleanup() {
    Game.currentLevel(null);
    Game.removeAllSystems();
    Game.removeAllEntities();
  }

  /** Test that the position is set to a random free tile if the position is illegal. */
  @Test
  public void test_illegalPosition() {
    LevelElement[][] elementsLayout =
        new LevelElement[][] {
          {LevelElement.FLOOR, LevelElement.WALL}, {LevelElement.WALL, LevelElement.WALL}
        };
    Tile[][] layout = new DungeonLevel(elementsLayout, DesignLabel.DEFAULT).layout();
    Mockito.when(level.layout()).thenReturn(layout);
    Mockito.when(level.size()).thenReturn(new Tuple<>(2, 2));
    Mockito.when(level.tileAt(Mockito.any(Coordinate.class)))
        .thenAnswer(
            invocation -> {
              Coordinate c = invocation.getArgument(0);
              return Optional.of(layout[c.y()][c.x()]);
            });
    pc.position(PositionComponent.ILLEGAL_POSITION);

    system.execute();
    assertEquals(point, pc.position());
  }

  /** Test that the position is not changed if the position is legal. */
  @Test
  public void test_legalPosition() {
    pc.position(new Point(2, 2));
    system.execute();
    assertNotEquals(pc.position(), point);
  }
}
