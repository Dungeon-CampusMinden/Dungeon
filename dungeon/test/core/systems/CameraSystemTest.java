package core.systems;

import static org.junit.jupiter.api.Assertions.*;

import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.platform.gdx.systems.GdxCameraSystem;
import core.utils.Point;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link GdxCameraSystem} class. */
public class CameraSystemTest {

  private static final Point testPoint = new Point(3, 3);
  private final ILevel level = Mockito.mock(ILevel.class);
  private final Tile startTile = Mockito.mock(Tile.class);
  private GdxCameraSystem gdxCameraSystem;
  private Point expectedFocusPoint;

  /** WTF? . */
  @BeforeEach
  public void setup() {
    gdxCameraSystem = new GdxCameraSystem();
    Game.add(gdxCameraSystem);
    Mockito.when(startTile.position()).thenReturn(testPoint);
    Mockito.when(level.randomTilePoint(Mockito.any())).thenReturn(Optional.of(testPoint));
    Mockito.when(level.startTile()).thenReturn(Optional.of(startTile));
    Game.add(new LevelSystem());
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
    // reset Camera
    GdxCameraSystem.camera().viewportWidth = GdxCameraSystem.viewportWidth();
    GdxCameraSystem.camera().viewportHeight = GdxCameraSystem.viewportHeight();
    GdxCameraSystem.camera().position.set(0, 0, 0);
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

    gdxCameraSystem.execute();
    assertEquals(expectedFocusPoint.x(), GdxCameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y(), GdxCameraSystem.camera().position.y, 0.001);
  }

  /** WTF? . */
  @Test
  public void executeWithoutEntity() {
    Game.currentLevel(level);

    expectedFocusPoint = level.startTile().orElseThrow().position();

    gdxCameraSystem.execute();

    assertEquals(expectedFocusPoint.x(), GdxCameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y(), GdxCameraSystem.camera().position.y, 0.001);
  }

  /** WTF? . */
  @Test
  public void executeWithoutLevel() {
    Game.currentLevel(null);
    Point expectedFocusPoint = new Point(0, 0);
    gdxCameraSystem.execute();
    assertEquals(expectedFocusPoint.x(), GdxCameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y(), GdxCameraSystem.camera().position.y, 0.001);
  }

  /**
   * Positive test for {@link GdxCameraSystem#isPointInFrustum(Point)}.
   *
   * <p>It checks if a point within the camera's frustum is correctly identified as visible.
   */
  @Test
  public void isPointInFrustumWithVisiblePoint() {
    float x = 1.0f;
    float y = 1.0f;
    assertTrue(GdxCameraSystem.isPointInFrustum(new Point(x, y)));
  }

  /**
   * Negative test for {@link GdxCameraSystem#isPointInFrustum(Point)}.
   *
   * <p>It checks if a point outside the camera's frustum is correctly identified as not visible.
   */
  @Test
  public void isPointInFrustumWithInvisiblePoint() {
    float x = 100.0f;
    float y = 100.0f;
    assertFalse(GdxCameraSystem.isPointInFrustum(new Point(x, y)));
  }
}
