package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * System responsible for updating the position of entities based on their velocity, while
 * considering collision and accessibility of the target tiles.
 *
 * <p>The system processes all entities that have both {@link VelocityComponent} and {@link
 * PositionComponent}. It limits velocity to a maximum speed, applies movement, and handles
 * collisions with non-accessible tiles, including special handling for pit tiles if the entity is
 * allowed to enter them.
 */
public class MoveSystem extends System {

  public static final float FLUSH_OFFSET = 0.001f;

  /**
   * Constructs a MoveSystem that requires entities to have {@link VelocityComponent} and {@link
   * PositionComponent}.
   */
  public MoveSystem() {
    super(VelocityComponent.class, PositionComponent.class);
  }

  /**
   * Executes the system logic for all filtered entities.
   *
   * <p>For each entity, calculates the new position based on the current velocity, caps the
   * velocity to the maxSpeed, and updates the position if the target tile is accessible. If the
   * target tile is blocked, tries moving along individual axes to allow sliding along walls and
   * calls the onWallHit callback.
   */
  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::updatePosition);
  }

  /**
   * Updates the position of a single entity based on its velocity and collision rules.
   *
   * <p>The velocity is capped at the entity's maxSpeed to prevent moving too fast diagonally. The
   * method checks the accessibility of all corners of the entity's collision box and handles
   * sliding along walls by checking individual axis movement.
   *
   * <p>If the tile is blocked, the entity's {@code onWallHit} callback is invoked.
   *
   * @param data a record containing the entity and its required components
   */
  private void updatePosition(MSData data) {
    Vector2 velocity = data.vc.currentVelocity();

    // Cap velocity magnitude to maxSpeed, mainly for diagonal movement
    if (velocity.length() > data.vc.maxSpeed()) {
      velocity = velocity.normalize().scale(data.vc.maxSpeed());
    }

    // Calculate scaled velocity vector per frame time
    Vector2 sv = velocity.scale(1f / Game.frameRate());

    Point oldPos = data.pc.position();
    Point newPos = oldPos.translate(sv);

    boolean canEnterOpenPits = data.vc.canEnterOpenPits();

    // Helper to get all corners of the hitbox at a given position
    Vector2 offset = data.vc.moveboxOffset();
    Vector2 size = data.vc.moveboxSize();
    List<Vector2> hitboxCorners = new ArrayList<>();
    hitboxCorners.add(Vector2.of(offset.x(), offset.y())); // bottom-left
    hitboxCorners.add(Vector2.of(offset.x() + size.x(), offset.y())); // bottom-right
    hitboxCorners.add(Vector2.of(offset.x() + size.x(), offset.y() + size.y())); // top-right
    hitboxCorners.add(Vector2.of(offset.x(), offset.y() + size.y())); // top-left

    // Check if all corners are accessible from a given Point
    Predicate<Point> allCornersAccessible =
        pos ->
            hitboxCorners.stream()
                .allMatch(
                    v ->
                        isAccessible(Game.tileAt(pos.translate(v)).orElse(null), canEnterOpenPits));

    if (allCornersAccessible.test(newPos)) {
      data.pc.position(newPos);
    } else {
      // Try moving only along x or y axis for wall sliding
      Point xMove = new Point(newPos.x(), oldPos.y());
      Point yMove = new Point(oldPos.x(), newPos.y());

      boolean xAccessible = allCornersAccessible.test(xMove);
      boolean yAccessible = allCornersAccessible.test(yMove);

      // If only one axis movement is possible, we can determine which side the collision happened
      // on and move the entity to be flush with the wall on that side.
      if (xAccessible) {
        Tile tileAtBottomLeft = Game.tileAt(newPos.translate(hitboxCorners.get(0))).orElse(null);
        if (isAccessible(tileAtBottomLeft, canEnterOpenPits)) {
          xMove =
              new Point(
                  xMove.x(),
                  (float) Math.ceil(xMove.y() + size.y() + offset.y())
                      - size.y()
                      - offset.y()
                      - FLUSH_OFFSET);
        } else {
          xMove = new Point(xMove.x(), (float) Math.floor(xMove.y() + offset.y()) - offset.y());
        }
        data.pc.position(xMove);
      } else if (yAccessible) {
        Tile tileAtBottomLeft = Game.tileAt(newPos.translate(hitboxCorners.get(0))).orElse(null);
        if (isAccessible(tileAtBottomLeft, canEnterOpenPits)) {
          yMove =
              new Point(
                  (float) Math.ceil(yMove.x() + size.x() + offset.x())
                      - size.x()
                      - offset.x()
                      - FLUSH_OFFSET,
                  yMove.y());
        } else {
          yMove = new Point((float) Math.floor(yMove.x() + offset.x()) - offset.x(), yMove.y());
        }

        data.pc.position(yMove);
      } else {
        // Neither is accessible. Figure out which corner the player is running into, then move them
        // into that corner
        boolean movingRight = sv.x() > 0;
        boolean movingUp = sv.y() > 0;

        float newX, newY;
        if (movingRight) {
          newX =
              (float) Math.ceil(oldPos.x() + offset.x() + size.x())
                  - size.x()
                  - offset.x()
                  - FLUSH_OFFSET;
        } else {
          newX = (float) Math.floor(oldPos.x() + offset.x()) - offset.x();
        }

        if (movingUp) {
          newY =
              (float) Math.ceil(oldPos.y() + offset.y() + size.y())
                  - size.y()
                  - offset.y()
                  - FLUSH_OFFSET;
        } else {
          newY = (float) Math.floor(oldPos.y() + offset.y()) - offset.y();
        }

        data.pc.position(new Point(newX, newY));
      }

      // Notify entity that it hit a wall
      data.vc.onWallHit().accept(data.e);
    }
  }

  /**
   * Checks whether the path between two points is completely accessible by stepping along the
   * vector between them in small increments.
   *
   * <p>This method simulates movement from the starting point to the target by walking small steps
   * along the direction vector. At each step, it checks whether the tile is accessible or can be
   * entered (e.g., if it's a pit and the entity is allowed to enter pits).
   *
   * <p>This ensures that no wall or inaccessible tile is skipped due to large velocity steps,
   * especially important when moving diagonally or at high speeds.
   *
   * @param from the starting point
   * @param to the target point
   * @param canEnterPitTiles whether the entity is allowed to walk into pit tiles
   * @return true if the entire path from start to target is clear; false if a tile in between is
   *     blocked
   */
  boolean isPathClearByStepping(Point from, Point to, boolean canEnterPitTiles) {
    Vector2 direction = from.vectorTo(to);
    double distance = direction.length();

    if (distance == 0f) return true;

    // Choose a small step size to ensure all intermediate tiles are checked (including diagonals)
    Vector2 step = direction.normalize().scale(0.1f);
    Point current = from;

    // Step from start to end and check each tile along the way
    for (float traveled = 0; traveled <= distance; traveled += step.length()) {
      Tile tile = Game.tileAt(current).orElse(null);
      if (!isAccessible(tile, canEnterPitTiles)) {
        return false;
      }
      current = current.translate(step);
    }

    // Ensure that the final destination tile is also checked
    return isAccessible(Game.tileAt(to).orElse(null), canEnterPitTiles);
  }

  /**
   * Helper method to determine if a tile can be entered by the entity.
   *
   * <p>Considers both whether the tile is accessible and whether the entity is allowed to enter pit
   * tiles.
   *
   * @param tile the tile to check for accessibility
   * @param canEnterPitTiles whether the entity can enter pit tiles
   * @return true if tile is accessible or a pit tile that can be entered, false otherwise
   */
  private boolean isAccessible(Tile tile, boolean canEnterPitTiles) {
    return tile != null
        && (tile.isAccessible()
            || (canEnterPitTiles && tile.levelElement().equals(LevelElement.PIT)));
  }

  /**
   * Helper method to build the data object with the necessary components for processing.
   *
   * <p>Throws {@link MissingComponentException} if required components are missing.
   *
   * @param e the entity to build data from
   * @return a record containing the entity and its velocity and position components
   */
  private MSData buildDataObject(Entity e) {
    VelocityComponent vc =
        e.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, VelocityComponent.class));

    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    return new MSData(e, vc, pc);
  }

  /**
   * Data record bundling entity and its components for processing in this system.
   *
   * @param e the entity
   * @param vc the velocity component
   * @param pc the position component
   */
  private record MSData(Entity e, VelocityComponent vc, PositionComponent pc) {}
}
