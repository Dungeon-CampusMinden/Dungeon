package engine.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.CameraComponent;
import engine.components.PositionComponent;
import engine.game.PreRunConfiguration;
import engine.input.GameInput;
import engine.level.Tile;
import engine.utils.FrameSmoothing;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import feature.utils.EntityUtils;

/**
 * The CameraSystem sets the focus point of the game. It is responsible for what is visible on
 * screen.
 *
 * <p>The camera will follow an entity with a {@link CameraComponent}. If there is no entity with a
 * {@link CameraComponent}, the start tile of the current level will be in focus.
 *
 * <p>In {@link #isPointInFrustum(Point)} also checks if points are visible on screen and should be
 * rendered.
 *
 * <p>The zoom is requested through {@link #zoom(float, ZoomSnap)} and snapped every frame to a
 * whole number of screen pixels per art pixel, so pixel art stays sharp and even while the camera
 * moves. The snapped zoom depends on the window width and follows it when the window is resized.
 *
 * @see CameraComponent
 */
public final class CameraSystem extends System {
  /** Zoom requested at start, the close-up the game is played in. */
  public static final float DEFAULT_ZOOM_FACTOR = 0.35f;

  /** Share of the remaining distance to the focus point the camera covers per world tick. */
  private static final float CAMERA_FOCUS_LERP = 0.2f;

  private static final float FIELD_WIDTH_AND_HEIGHT_IN_PIXEL = 16f;

  /** Slack for float rounding, so a zoom that lies exactly on a raster step stays on it. */
  private static final float RASTER_EPSILON = 1e-3f;

  private static final OrthographicCamera CAMERA =
      new OrthographicCamera(viewportWidth(), viewportHeight());

  private static ZoomRequest zoomRequest = new ZoomRequest(DEFAULT_ZOOM_FACTOR, ZoomSnap.NEAREST);

  private Point actualPosition;
  private Point focusPoint;

  static {
    applyZoom();
  }

  /**
   * How a requested zoom is fitted to the pixel raster: the zoom steps at which every art pixel
   * covers a whole number of screen pixels (1, 2, 3, ...), or, zoomed out further, at which a
   * screen pixel covers a whole number of art pixels (1/2, 1/3, ... pixels per art pixel).
   */
  public enum ZoomSnap {
    /** The raster step nearest to the requested zoom. */
    NEAREST,
    /** The nearest raster step that shows at least the requested area, so nothing gets cropped. */
    AT_LEAST,
    /** Exactly the requested zoom, off the raster; for animated zooms. */
    FREE
  }

  /**
   * A zoom as requested from {@link CameraSystem}, before snapping. Save it with {@link
   * #zoomRequest()} to restore it later.
   *
   * @param zoom the requested zoom, world units per viewport unit
   * @param snap how the zoom is fitted to the pixel raster
   */
  public record ZoomRequest(float zoom, ZoomSnap snap) {}

  /** Create a new {@link CameraSystem}. */
  public CameraSystem() {
    super(AuthoritativeSide.CLIENT, CameraComponent.class, PositionComponent.class);
  }

  static float viewportWidth() {
    return PreRunConfiguration.windowWidth()
        / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL; // Using PreRun to keep the same zoom
  }

  static float viewportHeight() {
    return PreRunConfiguration.windowHeight()
        / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL; // Using PreRun to keep the same zoom
  }

  /**
   * Checks if point (x,y) is probably visible on screen. Points that are not visible should not be
   * rendered.
   *
   * @param point The point to check if it is visible on screen.
   * @return True if the point is visible on screen, false otherwise.
   */
  public static boolean isPointInFrustum(Point point) {
    final float OFFSET = 1f;
    Point lowerLeft = point.translate(Vector2.of(-OFFSET, -OFFSET));
    Point upperRight = point.translate(Vector2.of(OFFSET, OFFSET));
    BoundingBox bounds =
        new BoundingBox(
            new Vector3(lowerLeft.x(), lowerLeft.y(), 0),
            new Vector3(upperRight.x(), upperRight.y(), 0));
    return CAMERA.frustum.boundsInFrustum(bounds);
  }

  /**
   * Getter for the camera.
   *
   * <p>Do not write its {@code zoom}: the system overwrites it every frame with the snapped zoom.
   * Use {@link #zoom(float)}, {@link #zoom(float, ZoomSnap)} or {@link #zoomBy(int)} instead.
   *
   * @return Orthographic Camera from the libGDX Framework
   */
  public static OrthographicCamera camera() {
    return CAMERA;
  }

  /**
   * Gets the zoom the camera renders with, after snapping.
   *
   * @return the rendered zoom, world units per viewport unit
   */
  public static float zoom() {
    return CAMERA.zoom;
  }

  /**
   * Requests a zoom snapped to the nearest raster step; see {@link ZoomSnap#NEAREST}.
   *
   * @param zoom the requested zoom, world units per viewport unit; larger shows more
   */
  public static void zoom(float zoom) {
    zoom(zoom, ZoomSnap.NEAREST);
  }

  /**
   * Requests a zoom. The camera keeps the request and snaps it again every frame, so it follows
   * changes of the window width.
   *
   * @param zoom the requested zoom, world units per viewport unit; larger shows more
   * @param snap how the zoom is fitted to the pixel raster
   */
  public static void zoom(float zoom, ZoomSnap snap) {
    zoomRequest = new ZoomRequest(zoom, snap);
    applyZoom();
  }

  /**
   * Gets the zoom last requested, to restore it later with {@link #zoom(float, ZoomSnap)}.
   *
   * @return the request before snapping
   */
  public static ZoomRequest zoomRequest() {
    return zoomRequest;
  }

  /**
   * Moves the rendered zoom by whole raster steps and requests the result; for zoom keys and the
   * mouse wheel.
   *
   * @param steps raster steps; positive enlarges the picture (shows less), negative shows more
   */
  public static void zoomBy(int steps) {
    zoom(steppedZoom(pixelWidth(), viewportWidth(), zoom(), steps));
  }

  /**
   * Checks if the given entity is hovered by the mouse cursor.
   *
   * <p>Uses {@link EntityUtils#isPointOverEntity(Entity, Point)} so debug hover and interaction
   * targeting use the same entity shape.
   *
   * <p>Returns false if the entity does not have a {@link PositionComponent PositionComponent} or
   * if the input or graphics context is not available (e.g., in headless mode).
   *
   * @param entity The entity to check.
   * @return True if the entity is hovered, false otherwise.
   */
  public static boolean isEntityHovered(Entity entity) {
    if (Game.isHeadless() || entity.fetch(PositionComponent.class).isEmpty()) {
      return false;
    }

    engine.utils.Vector2 pointer = GameInput.pointer();
    Vector3 mousePos = CAMERA.unproject(new Vector3(pointer.x(), pointer.y(), 0));
    Point mousePoint = new Point(mousePos.x, mousePos.y);

    return EntityUtils.isPointOverEntity(entity, mousePoint);
  }

  @Override
  public void execute() {
    filteredEntityStream(CameraComponent.class, PositionComponent.class)
        .filter(CameraSystem::hasLegalPosition)
        .findAny()
        .ifPresentOrElse(this::focus, this::focus);

    approachFocusPoint();
    CAMERA.update();
  }

  /** Jumps to the current focus on the next execution instead of panning toward it. */
  public void snapToFocus() {
    actualPosition = null;
  }

  private static boolean hasLegalPosition(Entity entity) {
    return entity
        .fetch(PositionComponent.class)
        .map(position -> !position.position().equals(PositionComponent.ILLEGAL_POSITION))
        .orElse(false);
  }

  @Override
  public void render(final float delta) {
    float aspectRatio = Game.windowWidth() / (float) Game.windowHeight();
    CAMERA.viewportWidth = viewportWidth();
    CAMERA.viewportHeight = viewportWidth() / aspectRatio;
    applyZoom();
  }

  private static void applyZoom() {
    CAMERA.zoom =
        snappedZoom(pixelWidth(), viewportWidth(), zoomRequest.zoom(), zoomRequest.snap());
  }

  // Width of the back buffer, which has more pixels than the window on HiDPI screens; 0 headless.
  private static float pixelWidth() {
    return Gdx.graphics == null ? 0 : Gdx.graphics.getBackBufferWidth();
  }

  /**
   * Fits a zoom to the pixel raster.
   *
   * @param pixelWidth width of the screen in pixels; without a screen (0) the zoom stays as it is
   * @param tilesAtZoomOne tiles visible across the screen at zoom 1
   * @param zoom the requested zoom
   * @param snap how to fit it
   * @return the zoom of the chosen raster step, or {@code zoom} for {@link ZoomSnap#FREE}
   */
  static float snappedZoom(float pixelWidth, float tilesAtZoomOne, float zoom, ZoomSnap snap) {
    if (!(pixelWidth > 0 && tilesAtZoomOne > 0 && zoom > 0)) return zoom;
    float pixels = pixelsPerArtPixel(pixelWidth, tilesAtZoomOne, zoom);
    return switch (snap) {
      case NEAREST -> rasterZoom(pixelWidth, tilesAtZoomOne, nearestStep(pixels));
      case AT_LEAST ->
          rasterZoom(
              pixelWidth,
              tilesAtZoomOne,
              pixels >= 1
                  ? (int) Math.floor(pixels + RASTER_EPSILON)
                  : 2 - (int) Math.ceil(1 / pixels - RASTER_EPSILON));
      case FREE -> zoom;
    };
  }

  /**
   * Moves a zoom by whole raster steps, starting from the step nearest to it.
   *
   * @param pixelWidth width of the screen in pixels; without a screen (0) the zoom stays as it is
   * @param tilesAtZoomOne tiles visible across the screen at zoom 1
   * @param zoom the zoom to start from
   * @param steps raster steps; positive enlarges the picture
   * @return the zoom of the reached raster step
   */
  static float steppedZoom(float pixelWidth, float tilesAtZoomOne, float zoom, int steps) {
    if (!(pixelWidth > 0 && tilesAtZoomOne > 0 && zoom > 0)) return zoom;
    int step = nearestStep(pixelsPerArtPixel(pixelWidth, tilesAtZoomOne, zoom)) + steps;
    return rasterZoom(pixelWidth, tilesAtZoomOne, step);
  }

  private static float pixelsPerArtPixel(float pixelWidth, float tilesAtZoomOne, float zoom) {
    return pixelWidth / (tilesAtZoomOne * zoom * FIELD_WIDTH_AND_HEIGHT_IN_PIXEL);
  }

  // Raster steps count screen pixels per art pixel from 1 upwards, and art pixels per screen pixel
  // from 1 downwards: step 3 is 3 pixels per art pixel, step 0 is 1/2, step -1 is 1/3.
  private static int nearestStep(float pixels) {
    return pixels >= 1 ? Math.round(pixels) : 2 - Math.round(1 / pixels);
  }

  private static float rasterZoom(float pixelWidth, float tilesAtZoomOne, int step) {
    float pixels = step >= 1 ? step : 1f / (2 - step);
    return pixelWidth / (tilesAtZoomOne * pixels * FIELD_WIDTH_AND_HEIGHT_IN_PIXEL);
  }

  private void approachFocusPoint() {
    if (actualPosition == null) {
      actualPosition = focusPoint;
    }
    float lerp = FrameSmoothing.share(CAMERA_FOCUS_LERP);
    float newX = actualPosition.x() * (1 - lerp) + (focusPoint.x() * lerp);
    float newY = actualPosition.y() * (1 - lerp) + (focusPoint.y() * lerp);
    actualPosition = new Point(newX, newY);

    if (actualPosition.distance(focusPoint) <= 0.01f) {
      actualPosition = focusPoint;
    }

    CAMERA.position.set(actualPosition.x(), actualPosition.y(), 0);
  }

  private void focus() {
    Point focusPoint;
    if (Game.currentLevel().isEmpty()) focusPoint = new Point(0, 0);
    else focusPoint = Game.startTile().map(Tile::position).orElse(new Point(0, 0));

    focus(focusPoint);
  }

  private void focus(Entity entity) {
    // The camera follows the entity where it is drawn, or the two would drift apart between ticks.
    PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
    focus(EntityUtils.getPosition(entity).translate(pc.position().vectorTo(pc.renderPosition())));
  }

  private void focus(Point point) {
    focusPoint = point;
  }

  @Override
  public void stop() {
    // Cant be stopped
  }

  /**
   * Gets the world bounds of the camera.
   *
   * @return The world bounds of the camera as a Rectangle.
   */
  public static Rectangle getCameraWorldBounds() {
    float worldWidth = camera().viewportWidth * camera().zoom;
    float worldHeight = camera().viewportHeight * camera().zoom;
    float camX = camera().position.x;
    float camY = camera().position.y;
    float posX = camX - (worldWidth / 2f);
    float posY = camY - (worldHeight / 2f);
    return new Rectangle(worldWidth, worldHeight, posX, posY);
  }
}
