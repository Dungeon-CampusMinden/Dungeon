package engine.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.CameraComponent;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.utils.Point;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link CameraSystem} class. */
public class CameraSystemTest {

  private static final Point testPoint = new Point(3, 3);
  private final ILevel level = Mockito.mock(ILevel.class);
  private final Tile startTile = Mockito.mock(Tile.class);
  private CameraSystem cameraSystem;
  private Point expectedFocusPoint;

  /** WTF? . */
  @BeforeEach
  public void setup() {
    cameraSystem = new CameraSystem();
    Game.add(cameraSystem);
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
    CameraSystem.camera().viewportWidth = CameraSystem.viewportWidth();
    CameraSystem.camera().viewportHeight = CameraSystem.viewportHeight();
    CameraSystem.camera().position.set(0, 0, 0);
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
    assertEquals(expectedFocusPoint.x(), CameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y(), CameraSystem.camera().position.y, 0.001);
  }

  /** WTF? . */
  @Test
  public void executeWithoutEntity() {
    Game.currentLevel(level);

    expectedFocusPoint = level.startTile().orElseThrow().position();

    cameraSystem.execute();

    assertEquals(expectedFocusPoint.x(), CameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y(), CameraSystem.camera().position.y, 0.001);
  }

  /** WTF? . */
  @Test
  public void executeWithoutLevel() {
    Game.currentLevel(null);
    Point expectedFocusPoint = new Point(0, 0);
    cameraSystem.execute();
    assertEquals(expectedFocusPoint.x(), CameraSystem.camera().position.x, 0.001);
    assertEquals(expectedFocusPoint.y(), CameraSystem.camera().position.y, 0.001);
  }

  /**
   * Positive test for {@link CameraSystem#isPointInFrustum(Point)}.
   *
   * <p>It checks if a point within the camera's frustum is correctly identified as visible.
   */
  @Test
  public void isPointInFrustumWithVisiblePoint() {
    float x = 1.0f;
    float y = 1.0f;
    assertTrue(CameraSystem.isPointInFrustum(new Point(x, y)));
  }

  /**
   * Negative test for {@link CameraSystem#isPointInFrustum(Point)}.
   *
   * <p>It checks if a point outside the camera's frustum is correctly identified as not visible.
   */
  @Test
  public void isPointInFrustumWithInvisiblePoint() {
    float x = 100.0f;
    float y = 100.0f;
    assertFalse(CameraSystem.isPointInFrustum(new Point(x, y)));
  }

  // A window of the start size: 1280 pixels across 80 tiles at zoom 1.
  private static final float START_WIDTH = 1280;
  private static final float START_TILES = 80;

  /** The default zoom of 0.35 renders at 3 screen pixels per art pixel in a start-size window. */
  @Test
  public void snappedZoomDefaultIsThreePixelsPerArtPixel() {
    float zoom =
        CameraSystem.snappedZoom(
            START_WIDTH,
            START_TILES,
            CameraSystem.DEFAULT_ZOOM_FACTOR,
            CameraSystem.ZoomSnap.NEAREST);
    assertEquals(1 / 3f, zoom, 1e-5);
  }

  /** {@code AT_LEAST} never shows less than requested, even where the nearest step would crop. */
  @Test
  public void snappedZoomAtLeastNeverCrops() {
    assertEquals(
        0.5f,
        CameraSystem.snappedZoom(START_WIDTH, START_TILES, 0.35f, CameraSystem.ZoomSnap.AT_LEAST),
        1e-5);
    for (float requested = 0.05f; requested < 4; requested += 0.01f) {
      float zoom =
          CameraSystem.snappedZoom(
              START_WIDTH, START_TILES, requested, CameraSystem.ZoomSnap.AT_LEAST);
      assertTrue(zoom >= requested * (1 - 1e-3f), requested + " -> " + zoom);
    }
  }

  /**
   * A fifth more than the rendered zoom, fitted with {@code AT_LEAST}, is a raster step further out
   * at every window width; Soulweaver's follow camera relies on it.
   */
  @Test
  public void atLeastAFifthMoreThanRenderedStepsOut() {
    for (float width = 300; width <= 4000; width += 1) {
      float rendered =
          CameraSystem.snappedZoom(
              width, START_TILES, CameraSystem.DEFAULT_ZOOM_FACTOR, CameraSystem.ZoomSnap.NEAREST);
      float further =
          CameraSystem.snappedZoom(
              width, START_TILES, rendered * 1.2f, CameraSystem.ZoomSnap.AT_LEAST);
      assertTrue(further > rendered * 1.1f, width + ": " + rendered + " -> " + further);
    }
  }

  /** {@code FREE} keeps the requested zoom off the raster. */
  @Test
  public void snappedZoomFreePassesThrough() {
    assertEquals(
        0.35f,
        CameraSystem.snappedZoom(START_WIDTH, START_TILES, 0.35f, CameraSystem.ZoomSnap.FREE));
  }

  /** Zoomed out beyond one pixel per art pixel, the zoom snaps to 1/2, 1/3, ... of a pixel. */
  @Test
  public void snappedZoomZoomedOutSnapsToFractions() {
    assertEquals(
        2f,
        CameraSystem.snappedZoom(START_WIDTH, START_TILES, 2.4f, CameraSystem.ZoomSnap.NEAREST),
        1e-5);
    assertEquals(
        3f,
        CameraSystem.snappedZoom(START_WIDTH, START_TILES, 2.6f, CameraSystem.ZoomSnap.NEAREST),
        1e-5);
  }

  /** Stepping crosses from one pixel per art pixel to half a pixel and back to two. */
  @Test
  public void steppedZoomCrossesOnePixelPerArtPixel() {
    assertEquals(2f, CameraSystem.steppedZoom(START_WIDTH, START_TILES, 1f, -1), 1e-5);
    assertEquals(0.5f, CameraSystem.steppedZoom(START_WIDTH, START_TILES, 1f, 1), 1e-5);
  }
}
