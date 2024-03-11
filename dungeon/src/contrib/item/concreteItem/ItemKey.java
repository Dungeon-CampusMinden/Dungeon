package contrib.item.concreteItem;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.WorldItemBuilder;
import contrib.item.Item;
import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.FloorTile;
import core.utils.Point;
import core.utils.components.draw.Animation;

public class ItemKey extends Item {
    private LevelNode thisRoom;
    private LevelNode nextRoom;


    public ItemKey(String displayName, String description, Animation inventoryAnimation, Animation worldAnimation) {
        super(displayName, description, inventoryAnimation, worldAnimation);
    }


    public void setThisRoom(LevelNode thisRoom) {
        this.thisRoom = thisRoom;
    }

    public void setNextRoom(LevelNode nextRoom) {
        this.nextRoom = nextRoom;
    }

    @Override
    public boolean drop(final Point position) {
        if (Game.tileAT(position) instanceof FloorTile) {
            Entity droppedKey = WorldItemBuilder.buildWorldItem(this, position);

            droppedKey.add(new InteractionComponent(
                1,
                false,
                (interacted, interactor) -> {
                    if (!interactor.fetch(InventoryComponent.class).orElseThrow().hasItem(this)) {
                        GeneratorUtils.doorAt(thisRoom.level(), Direction.SOUTH).orElseThrow().open();
                        GeneratorUtils.doorAt(nextRoom.level(), Direction.NORTH).orElseThrow().open();
                    }

                    interactor.fetch(InventoryComponent.class).orElseThrow().add(this);
                    Game.remove(droppedKey);
                }
            ));

            Game.add(droppedKey);
            return true;
        }
        return false;
    }
}
