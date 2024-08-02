package item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import java.io.IOException;

/**
 * A Egg that spawns a monster on usage.
 *
 * <p>Can be used for crafting.
 */
public class ItemResourceEgg extends contrib.item.concreteItem.ItemResourceEgg {

  static {
    Item.registerItem(ItemResourceEgg.class);
  }

  /** Create a new Egg. */
  public ItemResourceEgg() {
    super();
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              try {
                Entity monster = EntityFactory.randomMonster();
                monster
                    .fetch(PositionComponent.class)
                    .orElseThrow()
                    .position(e.fetch(PositionComponent.class).orElseThrow().position());
                monster
                    .fetch(HealthComponent.class)
                    .ifPresent(
                        hc -> {
                          hc.currentHealthpoints(1);
                          hc.maximalHealthpoints(1);
                        });
                Game.add(monster);
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });
  }
}
