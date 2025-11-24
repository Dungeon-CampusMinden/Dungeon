package core.systems;

import static org.junit.jupiter.api.Assertions.*;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.FloorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
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
