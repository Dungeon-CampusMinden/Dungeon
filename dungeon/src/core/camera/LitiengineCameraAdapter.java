package core.camera;

import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.level.Tile;
import core.platform.CameraAdapter;
import core.platform.Platform;
import core.utils.Point;
import java.util.Optional;

/** Camera adapter for the LITIENGINE backend. */
public final class LitiengineCameraAdapter implements CameraAdapter {

  @Override
  public boolean supportsZoom() {
    return true;
  }

  @Override
  public float zoom() {
    return LitiengineCameraState.zoom();
  }

  @Override
  public void zoom(float zoom) {
    LitiengineCameraState.zoom(zoom);
  }

  @Override
  public boolean supportsFocusPosition() {
    return true;
  }

  @Override
  public Point focusPosition() {
    return LitiengineCameraState.focusPosition();
  }

  @Override
  public void focusPosition(Point focusPosition) {
    LitiengineCameraState.focusPosition(focusPosition);
  }

  @Override
  public boolean supportsFollowTargetResolution() {
    return true;
  }

  @Override
  public Point resolveFollowTarget() {
    Optional<Point> playerPos =
      Game.player()
        .flatMap(player -> player.fetch(PositionComponent.class))
        .map(PositionComponent::position);

    Optional<Point> cameraComponentPos =
      ECSManagement.levelEntities()
        .filter(entity -> entity.isPresent(CameraComponent.class))
        .findFirst()
        .flatMap(entity -> entity.fetch(PositionComponent.class))
        .map(PositionComponent::position);

    Optional<Point> trackedPoint = playerPos.isPresent() ? playerPos : cameraComponentPos;
    Optional<Point> levelStartPoint =
      Game.currentLevel().isPresent() ? Game.startTile().map(Tile::position) : Optional.empty();

    return CameraMath.resolveFocus(trackedPoint, levelStartPoint);
  }

  @Override
  public boolean supportsViewportMetrics() {
    return true;
  }

  @Override
  public float viewportWidth() {
    return LitiengineCameraViews.activeView()
      .map(view -> Platform.window().width() / (float) Math.max(1, view.tilePx()))
      .orElse(0f);
  }

  @Override
  public float viewportHeight() {
    return LitiengineCameraViews.activeView()
      .map(view -> Platform.window().height() / (float) Math.max(1, view.tilePx()))
      .orElse(0f);
  }
}
