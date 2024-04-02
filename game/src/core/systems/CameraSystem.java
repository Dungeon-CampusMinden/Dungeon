package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import core.Entity;
import core.Game;
import core.System;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.game.PreRunConfiguration;
import core.utils.Point;
import core.utils.components.MissingComponentException;

/**
 * The CameraSystem sets the focus point of the game. It is responsible for what is visible on
 * screen.
 *
 * <p>The camera will follow an entity with a {@link CameraComponent}. If there is no entity with a
 * {@link CameraComponent}, the start tile of the current level will be in focus.
 *
 * <p>In {@link #isPointInFrustum(float, float)} also checks if points are visible on screen and
 * should be rendered.
 *
 * @see CameraComponent
 */
public final class CameraSystem extends System {
  /** WTF? . */
  public static final float DEFAULT_ZOOM_FACTOR = 0.35f;

  private static final float FIELD_WIDTH_AND_HEIGHT_IN_PIXEL = 16f;
  private static final OrthographicCamera CAMERA =
      new OrthographicCamera(viewportWidth(), viewportHeight());

  static {
    camera().zoom = DEFAULT_ZOOM_FACTOR;
  }

  /** Create a new {@link CameraSystem}. */
  public CameraSystem() {
    super(CameraComponent.class, PositionComponent.class);
  }

  private static float viewportWidth() {
    return PreRunConfiguration.windowWidth() / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL;
  }

  private static float viewportHeight() {
    return PreRunConfiguration.windowHeight() / FIELD_WIDTH_AND_HEIGHT_IN_PIXEL;
  }

  /**
   * Checks if point (x,y) is probably visible on screen. Points that are not visible should not be
   * rendered.
   *
   * @param x WTF? .
   * @param y WTF? .
   * @return WTF? .
   */
  public static boolean isPointInFrustum(float x, float y) {
    final float OFFSET = 1f;
    BoundingBox bounds =
        new BoundingBox(
            new Vector3(x - OFFSET, y - OFFSET, 0), new Vector3(x + OFFSET, y + OFFSET, 0));
    return CAMERA.frustum.boundsInFrustum(bounds);
  }

  /**
   * Getter for the camera.
   *
   * @return Orthographic Camera from the libGDX Framework
   */
  public static OrthographicCamera camera() {
    return CAMERA;
  }

  @Override
  public void execute() {
    if (entityStream().findAny().isEmpty()) focus();
    else entityStream().forEach(this::focus);
    // Check if Gdx.graphics is null which happens when the game is run in headless mode (e.g.
    // in tests)
    if (Gdx.graphics != null) {
      float aspectRatio = Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
      CAMERA.viewportWidth = viewportWidth();
      CAMERA.viewportHeight = viewportWidth() / aspectRatio;
    }
    CAMERA.update();
  }

  private void focus() {
    Point focusPoint;
    if (Game.currentLevel() == null) focusPoint = new Point(0, 0);
    else focusPoint = Game.startTile().position();
    focus(focusPoint);
  }

  private void focus(Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    focus(pc.position());
  }

  private void focus(Point point) {
    CAMERA.position.set(point.x, point.y, 0);
  }
}
