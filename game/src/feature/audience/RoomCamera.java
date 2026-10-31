package feature.audience;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.CameraComponent;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.utils.LevelElement;
import engine.systems.CameraSystem;
import engine.utils.Point;
import java.util.Set;

/**
 * Keeps the whole room on the game screen. Instead of following the hero, the camera looks at the
 * middle of the level and zooms until every tile of it fits, so the audience sees every puzzle of
 * the room at once. A room adds the system in its client setup.
 *
 * <p>The camera follows the entity with a {@link CameraComponent}. The hero gets one when the
 * server spawns it, so the system removes it every frame and keeps the only one on a local anchor
 * in the middle of the room.
 */
public final class RoomCamera extends System {
  /** Free tiles around the room, so the overlay in the corners covers less of it. */
  private static final float MARGIN = 1f;

  private final Entity anchor = Entity.createLocalEntity("room-camera");
  private ILevel measured;
  private float width;
  private float height;

  /** Creates the system with its camera anchor. */
  public RoomCamera() {
    super(AuthoritativeSide.CLIENT);
    anchor.add(new PositionComponent(new Point(0, 0)));
    anchor.add(new CameraComponent());
  }

  @Override
  public void execute() {
    Game.entities(Set.of(CameraComponent.class))
        .filter(entity -> entity != anchor)
        .toList()
        .forEach(entity -> entity.remove(CameraComponent.class));
    if (Game.findEntityById(anchor.id()).isEmpty()) Game.add(anchor);
    Game.currentLevel()
        .ifPresent(
            level -> {
              if (level != measured) measure(level);
              if (width <= 0) return;
              float zoom =
                  Math.max(
                      (width + 2 * MARGIN) / CameraSystem.camera().viewportWidth,
                      (height + 2 * MARGIN) / CameraSystem.camera().viewportHeight);
              if (Float.isFinite(zoom)) CameraSystem.camera().zoom = zoom;
            });
  }

  // Puts the anchor in the middle of the tiles that belong to the room; skipped tiles do not.
  private void measure(ILevel level) {
    measured = level;
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (Tile[] row : level.layout()) {
      for (Tile tile : row) {
        if (tile == null || tile.levelElement() == LevelElement.SKIP) continue;
        minX = Math.min(minX, tile.coordinate().x());
        minY = Math.min(minY, tile.coordinate().y());
        maxX = Math.max(maxX, tile.coordinate().x());
        maxY = Math.max(maxY, tile.coordinate().y());
      }
    }
    if (minX > maxX) return;
    width = maxX - minX + 1;
    height = maxY - minY + 1;
    anchor
        .fetch(PositionComponent.class)
        .orElseThrow()
        .position(new Point(minX + width / 2, minY + height / 2));
  }
}
