package dojo.item;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.WorldItemBuilder;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.level.elements.tile.FloorTile;
import core.utils.Point;
import core.utils.components.draw.Animation;
import dojo.rooms.Room;

/** An implementation for an item that acts as a key and opens specific doors on pickup. */
public class ItemKey extends Item {
  private Room thisRoom;
  private Room nextRoom;

  /**
   * Create a new Key.
   *
   * @param displayName The display name of the item.
   * @param description The description of the item.
   * @param inventoryAnimation The inventory animation of the item.
   * @param worldAnimation The world animation of the item.
   * @param thisRoom The current room for accessing the door.
   * @param nextRoom The next room for accessing the door.
   */
  public ItemKey(
      String displayName,
      String description,
      Animation inventoryAnimation,
      Animation worldAnimation,
      Room thisRoom,
      Room nextRoom) {
    super(displayName, description, inventoryAnimation, worldAnimation);
    this.thisRoom = thisRoom;
    this.nextRoom = nextRoom;
  }

  /**
   * Special implementation of {@link Item#drop(Point)}
   *
   * <p>This method automatically assigns a new {@link InteractionComponent} to the dropped Entity
   * of the item that allows to open the doors of the assigned rooms {@link #thisRoom} and {@link
   * #nextRoom}
   *
   * @param position The position where the item should be dropped.
   * @return
   */
  @Override
  public boolean drop(final Point position) {
    if (Game.tileAT(position) instanceof FloorTile) {
      Entity droppedKey = WorldItemBuilder.buildWorldItem(this, position);

      droppedKey.add(
          new InteractionComponent(
              1,
              false,
              (interacted, interactor) -> {
                if (!interactor.fetch(InventoryComponent.class).orElseThrow().hasItem(this)) {
                  thisRoom.openDoors();
                }

                interactor.fetch(InventoryComponent.class).orElseThrow().add(this);
                Game.remove(droppedKey);
              }));

      Game.add(droppedKey);
      return true;
    }
    return false;
  }
}
