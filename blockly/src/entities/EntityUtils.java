package entities;

import contrib.utils.LevelUtils;
import core.Entity;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.components.MissingComponentException;

/** EntityUtils is a utility class that provides methods for entity-related operations. */
public class EntityUtils {
  /**
   * Checks if one entity can see another entity in its view range.
   *
   * @param entity The entity that is trying to see the other entity.
   * @param other The entity that is being seen.
   * @param viewDistance The view distance of the entity.
   * @return true if the entity can see the other entity, false otherwise.
   * @see LevelUtils#canSee(Coordinate, Coordinate, Direction)
   */
  public static boolean canEntitySeeOther(Entity entity, Entity other, int viewDistance) {
    PositionComponent entityPos =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    PositionComponent otherPos =
        other
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(other, PositionComponent.class));

    Coordinate entityCoordinate = entityPos.coordinate();
    Coordinate otherCoordinate = otherPos.coordinate();
    if (entityCoordinate.equals(otherCoordinate)) return true;
    Direction viewDirectionEntity = entityPos.viewDirection();

    return LevelUtils.canSee(entityCoordinate, otherCoordinate, viewDirectionEntity)
        && entityCoordinate.distance(otherCoordinate) < viewDistance;
  }
}
