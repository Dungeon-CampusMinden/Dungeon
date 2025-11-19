package mushRoom.modules.mushrooms;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;

public class MushroomFactory {

  public static Entity createMushroom(Point p, Mushrooms type, boolean poisonous) {
    Entity mushroom = new Entity();
    mushroom.add(new PositionComponent(p));
    mushroom.add(new MushroomComponent(poisonous));

    DrawComponent dc = new DrawComponent(new SimpleIPath(type.getTexturePath()));
    mushroom.add(dc);

    mushroom.add(
        new InteractionComponent(
            1.5f,
            true,
            (e, who) -> {
              who.fetch(InventoryComponent.class)
                  .ifPresent(
                      inventory -> {
                        inventory.add((Item) MushroomItem.createMushroomItem(type));
                        Game.remove(e);
                      });
            }));

    return mushroom;
  }
}
