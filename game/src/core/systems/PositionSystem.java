package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;

/**
 * The {@link PositionSystem} checks if an entity has an illegal position and then changes the
 * position to a random accessible position in the currently active level.
 *
 * <p>Entities with the {@link PositionComponent} will be processed by this system.
 *
 * <p>If the position of an entity is equal to {@link PositionComponent#ILLEGAL_POSITION}, the
 * position of the entity will be set to a random accessible tile in the current level.
 *
 * <p>Note: In most cases, the position of an entity equals {@link
 * PositionComponent#ILLEGAL_POSITION} during the first frame of the currently active level. This
 * occurs because sometimes entities are created before the level is loaded.
 */
public final class PositionSystem extends System {

  /** Create a new PositionSystem. */
  public PositionSystem() {
    super(PositionComponent.class);
  }

  @Override
  public void execute() {
    entityStream()
        .map(this::buildDataObject)
        .filter(data -> data.pc.position().equals(PositionComponent.ILLEGAL_POSITION))
        .forEach(this::randomPosition);
  }

  /**
   * Assigns a random position to the entity if its current position is illegal. The new position is
   * a random accessible tile in the current level.
   *
   * @param data The PSData object containing entity and position component information.
   */
  private void randomPosition(final PSData data) {
    if (Game.currentLevel() != null) {
      Coordinate randomPosition = Game.randomTile(LevelElement.FLOOR).coordinate();
      boolean otherEntityIsOnThisCoordinate =
          entityStream()
              .map(this::buildDataObject)
              .anyMatch(psData -> psData.pc().position().toCoordinate().equals(randomPosition));
      if (!otherEntityIsOnThisCoordinate) {
        Point position = randomPosition.toPoint();
        // place on center
        position.x += 0.5f;
        position.y += 0.5f;
        data.pc().position(position);
      } else randomPosition(data);
    }
  }

  private PSData buildDataObject(final Entity e) {

    PositionComponent pc =
        e.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PositionComponent.class));

    return new PSData(e, pc);
  }

  private record PSData(Entity e, PositionComponent pc) {}
}
