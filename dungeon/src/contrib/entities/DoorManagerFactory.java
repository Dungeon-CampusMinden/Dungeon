package contrib.entities;

import contrib.components.LeverComponent;
import contrib.components.PredicateComponent;
import contrib.utils.ICommand;
import contrib.utils.PredicateFactory;
import core.Entity;
import core.level.elements.tile.DoorTile;
import java.util.function.Supplier;

/**
 * Factory class for creating {@link Entity} objects that manage door operations based on lever
 * states.
 *
 * <p>The {@link DoorManagerFactory} provides static methods to create {@link Entity} instances that
 * control a door's state (open or closed) based on the logical combination of two levers' states.
 * The logic can be configured using logical operations such as AND, OR, XOR, IS, and NOT. Each
 * method returns an entity that associates the lever states with a door action (open/close).
 *
 * <p>Example usage:
 *
 * <pre>
 * DoorTile door = new DoorTile();
 * LeverComponent lever1 = new LeverComponent();
 * LeverComponent lever2 = new LeverComponent();
 * Entity entity = DoorManagerFactory.and(door, lever1, lever2);
 * </pre>
 */
public class DoorManagerFactory {

  /**
   * Creates an {@link Entity} that opens the door when both levers are in the "on" state (AND
   * operation).
   *
   * @param door the {@link DoorTile} to be controlled
   * @param lever1 the first {@link LeverComponent}
   * @param lever2 the second {@link LeverComponent}
   * @return an {@link Entity} that controls the door based on the AND operation of the lever states
   */
  public static Entity and(DoorTile door, LeverComponent lever1, LeverComponent lever2) {
    return doorOpener(door, PredicateFactory.and(lever1, lever2));
  }

  /**
   * Creates an {@link Entity} that opens the door when at least one lever is in the "on" state (OR
   * operation).
   *
   * @param door the {@link DoorTile} to be controlled
   * @param lever1 the first {@link LeverComponent}
   * @param lever2 the second {@link LeverComponent}
   * @return an {@link Entity} that controls the door based on the OR operation of the lever states
   */
  public static Entity or(DoorTile door, LeverComponent lever1, LeverComponent lever2) {
    return doorOpener(door, PredicateFactory.or(lever1, lever2));
  }

  /**
   * Creates an {@link Entity} that opens the door when exactly one lever is in the "on" state (XOR
   * operation).
   *
   * @param door the {@link DoorTile} to be controlled
   * @param lever1 the first {@link LeverComponent}
   * @param lever2 the second {@link LeverComponent}
   * @return an {@link Entity} that controls the door based on the XOR operation of the lever states
   */
  public static Entity xor(DoorTile door, LeverComponent lever1, LeverComponent lever2) {
    return doorOpener(door, PredicateFactory.xor(lever1, lever2));
  }

  /**
   * Creates an {@link Entity} that opens the door when the specified lever is in the "on" state (IS
   * operation).
   *
   * @param door the {@link DoorTile} to be controlled
   * @param lever the {@link LeverComponent} to control the door
   * @return an {@link Entity} that controls the door based on the IS operation of the lever state
   */
  public static Entity is(DoorTile door, LeverComponent lever) {
    return doorOpener(door, PredicateFactory.is(lever));
  }

  /**
   * Creates an {@link Entity} that opens the door when the specified lever is in the "off" state
   * (NOT operation).
   *
   * @param door the {@link DoorTile} to be controlled
   * @param lever the {@link LeverComponent} to control the door
   * @return an {@link Entity} that controls the door based on the NOT operation of the lever state
   */
  public static Entity not(DoorTile door, LeverComponent lever) {
    return doorOpener(door, PredicateFactory.not(lever));
  }

  /**
   * Helper method to create an {@link Entity} that opens or closes the door based on a given logic
   * predicate.
   *
   * <p>This will close the door initially.
   *
   * @param door the {@link DoorTile} to be controlled
   * @param logic the {@link Supplier<Boolean>} that provides the logic for opening/closing the door
   * @return an {@link Entity} that controls the door based on the specified logic
   */
  public static Entity doorOpener(DoorTile door, Supplier<Boolean> logic) {
    Entity e = new Entity();
    ICommand action =
        new ICommand() {
          @Override
          public void execute() {
            door.open();
          }

          @Override
          public void undo() {
            door.close();
          }
        };
    e.add(new PredicateComponent(logic, action));
    door.close();
    return e;
  }
}
