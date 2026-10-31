package feature.audience;

import engine.Component;
import engine.Entity;
import engine.components.PositionComponent;
import engine.utils.Point;
import java.util.function.BooleanSupplier;

/**
 * Marks an entity the audience can pick as a destination. A room that marks targets lets the
 * audience choose from a list of them instead of voting on single steps. The hero walks to the
 * chosen target on its own, turns to it and uses it.
 *
 * <p>For an entity with an {@code InteractionComponent}, the hero stops on a free tile next to it.
 * For any other entity, such as the one {@link #tile} creates for an exit, the hero walks onto its
 * tile. A target is in the list only while {@code available} says so and the hero can reach it, so
 * a closed door hides everything behind it.
 *
 * @param label name in the list on the devices and on the game screen; unique in the room
 * @param available whether the room offers the target right now, for example after a puzzle
 */
public record AudienceTarget(String label, BooleanSupplier available) implements Component {

  /**
   * Creates an entity that marks a tile as a target to walk onto, such as the tile behind a door.
   * Add it to the game like any other entity.
   *
   * @param tile origin of the tile
   * @param label name in the list
   * @param available whether the room offers the tile right now
   * @return the new entity
   */
  public static Entity tile(Point tile, String label, BooleanSupplier available) {
    Entity entity = new Entity("audience-target-" + label);
    entity.add(new PositionComponent(tile));
    entity.add(new AudienceTarget(label, available));
    return entity;
  }
}
