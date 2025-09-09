package level;

import coderunner.BlocklyCommands;
import contrib.systems.FogSystem;
import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.systems.CameraSystem;
import core.utils.Direction;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;

/**
 * Utility class for managing level-related features such as camera control, hero positioning, zoom
 * level, and fog of war.
 */
public class LevelManagementUtils {

  /**
   * Focuses the camera on a specific coordinate by removing all existing camera components and
   * placing a new camera at the given coordinate.
   *
   * @param coordinate The coordinate to focus the camera on.
   */
  public static void cameraFocusOn(Coordinate coordinate) {
    Game.levelEntities()
        .filter(e -> e.isPresent(CameraComponent.class))
        .forEach(entity -> entity.remove(CameraComponent.class));

    Entity focusPoint = new Entity("cameraFocusPoint");
    focusPoint.add(new PositionComponent(coordinate.toPoint()));
    focusPoint.add(new CameraComponent());
    Game.add(focusPoint);
  }

  /**
   * Focuses the camera on the hero entity. Removes existing camera components and adds a camera
   * component to the hero.
   *
   * @throws MissingHeroException if the hero entity is not present.
   */
  public static void cameraFocusHero() {
    Game.levelEntities()
        .filter(e -> e.isPresent(CameraComponent.class))
        .forEach(entity -> entity.remove(CameraComponent.class));
    Game.hero().orElseThrow(() -> new MissingHeroException()).add(new CameraComponent());
  }

  /**
   * Repositions the hero entity to the center of its current tile.
   *
   * @throws MissingHeroException if the hero entity is not present.
   * @throws MissingComponentException if the hero does not have a PositionComponent.
   */
  public static void centerHero() {
    Entity hero = Game.hero().orElseThrow(() -> new MissingHeroException());
    PositionComponent pc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    pc.toTileCorner();
  }

  /**
   * Sets the viewing direction of the hero.
   *
   * @param viewDirection The new viewing direction to be set for the hero.
   * @throws MissingHeroException if the hero entity is not present.
   */
  public static void heroViewDirection(Direction viewDirection) {
    Entity hero = Game.hero().orElseThrow(() -> new MissingHeroException());
    BlocklyCommands.turnEntity(hero, viewDirection);
  }

  /** Zooms the camera in by decreasing the zoom factor. */
  public static void zoomIn() {
    CameraSystem.camera().zoom -= 0.2f;
  }

  /** Zooms the camera out by increasing the zoom factor. */
  public static void zoomOut() {
    CameraSystem.camera().zoom += 0.2f;
  }

  /** Resets the camera zoom to the default zoom factor. */
  public static void zoomDefault() {
    CameraSystem.camera().zoom = CameraSystem.DEFAULT_ZOOM_FACTOR;
  }

  /**
   * Enables or disables the fog of war effect.
   *
   * @param active {@code true} to activate the fog, {@code false} to deactivate.
   */
  public static void fog(boolean active) {
    FogSystem fs = (FogSystem) Game.systems().get(FogSystem.class);
    if (fs != null) fs.active(active);
  }
}
