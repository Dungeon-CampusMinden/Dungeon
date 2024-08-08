package item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.hud.DialogUtils;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** A secret item that is rewarded to the player after defeating the final boss. */
public class ItemReward extends Item {
  private static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/resource/stone.png");

  static {
    Item.registerItem(ItemReward.class);
  }

  /** Constructs the final reward item. */
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
              DialogUtils.showTextPopup(
                  "Nothing happens... I mean what did you expect? It's just a boulder. Ehm, I mean a rock. It's not like it's going to start talking to you or something.",
                  "Trying to use the rock...");
            });
  }
}
