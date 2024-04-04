package item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.entities.DialogFactory;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public class ItemReward extends Item {
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/resource/stone.png");

  static {
    Item.ITEMS.put(ItemReward.class.getSimpleName(), ItemReward.class);
  }

  public ItemReward() {
    super(
        "A rock",
        "You did it! You have defeated the final boss and now you are rewarded with the ultimate reward. A boulder. Its not just a boulder it's A rock!",
        Animation.fromSingleImage(DEFAULT_TEXTURE));
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              DialogFactory.showTextPopup(
                  "Nothing happens... I mean what did you expect? It's just a boulder. Ehm, I mean a rock. It's not like it's going to start talking to you or something.",
                  "Trying to use the rock...");
            });
  }
}
