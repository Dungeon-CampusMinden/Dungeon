package mushRoom.modules.items;

import contrib.item.concreteItem.ItemHammer;
import core.Entity;
import core.Game;
import mushRoom.Sounds;

public class CustomHammerItem extends ItemHammer {

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }
}
