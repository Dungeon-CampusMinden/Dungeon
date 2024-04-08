package item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import item.effects.RegenerationEffect;

public class ItemPotionRegenerationPotion extends Item {
  public static final IPath DEFAULT_TEXTURE =
      new SimpleIPath("items/potion/regeneration_potion.png");
  private static final int DURATION = 20;
  private static final int HEAL_PER_SECOND = 1;

  static {
    Item.ITEMS.put(
        ItemPotionRegenerationPotion.class.getSimpleName(), ItemPotionRegenerationPotion.class);
  }

  private final RegenerationEffect regenerationEffect;

  public ItemPotionRegenerationPotion() {
    super(
        "Regeneration Potion",
        "A potion that heals you over time",
        Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.regenerationEffect = new RegenerationEffect(HEAL_PER_SECOND, DURATION);
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              this.regenerationEffect.applyRegeneration(e);
            });
  }
}
