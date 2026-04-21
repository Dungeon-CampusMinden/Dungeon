package core.game.render.sprite;

import core.Game;
import core.camera.CameraMath;
import core.camera.CameraState;
import core.level.Tile;
import core.level.elements.ILevel;
import core.platform.Platform;
import core.utils.Point;
import core.utils.Rectangle;
import java.util.Optional;

/**
 * CameraViewCalculator is responsible for determining the viewport and rendering bounds
 * for a camera in the game.
 *
 * <p>It calculates the visible tiles, pixel offsets, and other
 * camera-related parameters based on the current level, screen size, and camera state.
 *
 * <p>This ensures the game world is rendered appropriately within the user's viewport.
 *
 * <p>This class operates with a fixed tile size and uses optional smoothing for camera motion.
 * It also calculates margins to reduce rendering artifacts such as pop-in.
 */
final class CameraViewCalculator {
  private static final int BASE_TILE_PX = 32;
  private static final int MIN_TILE_PX = 8;

  // How many tiles beyond the viewport we still render (avoid pop-in).
  private static final int VIEW_MARGIN_TILES = 2;

  // Optional smoothing (0 = no smoothing, 1 = jump). Keep small and stable.
  private static final float CAMERA_LERP = 0.20f;

  CameraView calculate(Optional<ILevel> levelOpt, int screenW, int screenH) {
    final int levelHeight =
        levelOpt.map(level -> level.layout() != null ? level.layout().length : 0).orElse(0);
    final int tilePx = effectiveTilePx();

    final Point target = resolveCameraFollowTarget(levelOpt);
    CameraState.followTarget(target);

    final Point focus = CameraState.stepFocus(CAMERA_LERP);

    final Rectangle worldBounds =
        CameraMath.worldBounds(focus, screenW / (float) tilePx, screenH / (float) tilePx, 1f)
            .expand(VIEW_MARGIN_TILES);

    final int minTileX = (int) Math.floor(worldBounds.x());
    final int maxTileX = (int) Math.ceil(worldBounds.x() + worldBounds.width());
    final int minTileY = (int) Math.floor(worldBounds.y());
    final int maxTileY = (int) Math.ceil(worldBounds.y() + worldBounds.height());

    final double focusPxX = focus.x() * tilePx + (tilePx / 2.0);
    final double focusPxY =
        (levelHeight > 0)
            ? ((levelHeight - 1 - focus.y()) * tilePx + (tilePx / 2.0))
            : (focus.y() * tilePx + (tilePx / 2.0));

    final double offsetX = (screenW / 2.0) - focusPxX;
    final double offsetY = (screenH / 2.0) - focusPxY;

    return new CameraView(
        offsetX, offsetY, minTileX, maxTileX, minTileY, maxTileY, levelHeight, tilePx);
  }

  private Point resolveCameraFollowTarget(Optional<ILevel> levelOpt) {
    if (Platform.camera().supportsFollowTargetResolution()) {
      return Platform.camera().resolveFollowTarget();
    }

    return CameraMath.resolveFocus(
        Optional.empty(),
        levelOpt.isPresent() ? Game.startTile().map(Tile::position) : Optional.empty());
  }

  private static int effectiveTilePx() {
    return Math.max(MIN_TILE_PX, Math.round(BASE_TILE_PX * CameraState.zoom()));
  }
}
