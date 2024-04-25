package core.systems;

import static org.junit.Assert.*;

import com.badlogic.gdx.utils.GdxNativesLoader;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.utils.Point;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for the {@link CameraSystem} class. */
public class CameraSystemTest {

  private static final Point testPoint = new Point(3, 3);
  private final ILevel level = Mockito.mock(ILevel.class);
  private final Tile startTile = Mockito.mock(Tile.class);
  private CameraSystem cameraSystem;
  private Point expectedFocusPoint;

  /** WTF? . */
  @BeforeClass
  public static void initGDX() {
    GdxNativesLoader.load(); // load natives for headless testing
  }

  /** WTF? . */
  @Before
  public void setup() {
    cameraSystem = new CameraSystem();
    Game.add(cameraSystem);
    Mockito.when(startTile.position()).thenReturn(testPoint);
    Mockito.when(level.randomTilePoint(Mockito.any())).thenReturn(testPoint);
    Mockito.when(level.startTile()).thenReturn(startTile);
    Game.add(new LevelSystem(null, null, () -> {}));
  }

  /** WTF? . */
  @After
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** WTF? . */
  @Test
  public void executeWithEntity() {
    Game.currentLevel(level);
    Entity entity = new Entity();
    expectedFocusPoint = new Point(3, 3);
    PositionComponent positionComponent = new PositionComponent(expectedFocusPoint);
    entity.add(positionComponent);
    entity.add(new CameraComponent());

    cameraSystem.execute();
    assertEquals(expectedFocusPoint.x, CameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y, CameraSystem.camera().position.y, 0.001);
  }

  /** WTF? . */
  @Test
  public void executeWithoutEntity() {
    Game.currentLevel(level);

    expectedFocusPoint = level.startTile().position();

    cameraSystem.execute();

    assertEquals(expectedFocusPoint.x, CameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y, CameraSystem.camera().position.y, 0.001);
  }

  /** WTF? . */
  @Test
  public void executeWithoutLevel() {
    Game.currentLevel(null);
    Point expectedFocusPoint = new Point(0, 0);
    cameraSystem.execute();
    assertEquals(expectedFocusPoint.x, CameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y, CameraSystem.camera().position.y, 0.001);
  }

  /** WTF? . */
  @Test
  public void isPointInFrustumWithVisiblePoint() {
    float x = 1.0f;
    float y = 1.0f;
    assertTrue(CameraSystem.isPointInFrustum(x, y));
  }

  /** WTF? . */
  @Test
  public void isPointInFrustumWithInvisiblePoint() {
    float x = 100.0f;
    float y = 100.0f;
    assertFalse(CameraSystem.isPointInFrustum(x, y));
  }
}
