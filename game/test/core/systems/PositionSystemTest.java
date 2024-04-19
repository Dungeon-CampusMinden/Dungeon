package core.systems;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.FloorTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for the {@link PositionSystem} class. */
public class PositionSystemTest {

  private PositionSystem system;
  private final Tile mock = Mockito.mock(FloorTile.class);
  private final Point point = new Point(3, 3);
  private final ILevel level = Mockito.mock(ILevel.class);
  private Entity entity;
  private PositionComponent pc;

  /** WTF? . */
  @Before
  public void setup() {
    pc = new PositionComponent();
    Game.add(new LevelSystem(null, null, () -> {}));
    Game.currentLevel(level);
    system = new PositionSystem();
    Game.add(system);
    entity = new Entity();
    Game.add(entity);

    entity.add(pc);

    Mockito.when(level.randomTile(LevelElement.FLOOR)).thenReturn(mock);
    Mockito.when(mock.position()).thenReturn(point);
    Mockito.when(mock.coordinate()).thenReturn(new Coordinate(3, 3));
  }

  /** WTF? . */
  @After
  public void cleanup() {
    Game.removeAllSystems();
    Game.removeAllEntities();
    Game.currentLevel(null);
  }

  /** WTF? . */
  @Test
  public void test_illegalPosition() {
    pc.position(PositionComponent.ILLEGAL_POSITION);
    // entities will be placed in the center of a tile, so add the offset for check
    Point offsetPoint = new Point(point.x + 0.5f, point.y + 0.5f);
    system.execute();
    assertTrue(pc.position().equals(offsetPoint));
  }

  /** WTF? . */
  @Test
  public void test_legalPosition() {
    pc.position(new Point(2, 2));
    system.execute();
    assertFalse("Nothing should have changed", pc.position().equals(point));
  }
}
