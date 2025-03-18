package contrib.entities;

import contrib.components.LeverComponent;
import contrib.components.PredicateComponent;
import contrib.utils.ICommand;
import contrib.utils.Predicate;
import contrib.utils.PredicateFactory;
import core.Entity;
import core.level.elements.tile.DoorTile;

public class DoorManagerFactory {

  public static Entity and(DoorTile door, LeverComponent lever1, LeverComponent lever2) {
    return doorOpener(door, PredicateFactory.and(lever1, lever2));
  }

  public static Entity or(DoorTile door, LeverComponent lever1, LeverComponent lever2) {
    return doorOpener(door, PredicateFactory.or(lever1, lever2));
  }

  public static Entity xor(DoorTile door, LeverComponent lever1, LeverComponent lever2) {
    return doorOpener(door, PredicateFactory.xor(lever1, lever2));
  }

  public static Entity is(DoorTile door, LeverComponent lever) {
    return doorOpener(door, PredicateFactory.is(lever));
  }

  public static Entity not(DoorTile door, LeverComponent lever) {
    return doorOpener(door, PredicateFactory.not(lever));
  }

  public static Entity doorOpener(DoorTile door, Predicate logic) {
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
