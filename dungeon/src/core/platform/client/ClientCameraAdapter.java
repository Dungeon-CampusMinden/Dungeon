package core.platform.client;

import core.Game;
import core.camera.CameraMath;
import core.camera.CameraState;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.level.Tile;
import core.platform.adapters.CameraAdapter;
import core.utils.Point;
import java.util.Optional;

/**
 * The ClientCameraAdapter class is an implementation of the CameraAdapter interface.
 *
 * <p>It provides support for various camera operations such as zooming, focusing,
 * following targets, and determining viewport metrics.
 *
 * <p>This adapter interacts with the game's camera state and relevant components
 * to calculate and manage the camera's behavior based on the current game context.
 */
public final class ClientCameraAdapter implements CameraAdapter {

  @Override
  public boolean supportsZoom() {
    return true;
  }

  @Override
  public float zoom() {
    return CameraState.zoom();
  }

  @Override
  public void zoom(float zoom) {
    CameraState.zoom(zoom);
  }

  @Override
  public Point focusPosition() {
    return CameraState.focusPosition();
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
}
