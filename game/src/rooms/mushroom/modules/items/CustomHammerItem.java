package rooms.mushroom.modules.items;

import engine.Entity;
import feature.inventory.items.ItemHammer;
import rooms.mushroom.Sounds;

/** Adds a pickup sound to the Hammer item. */
public class CustomHammerItem extends ItemHammer {

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }
}
