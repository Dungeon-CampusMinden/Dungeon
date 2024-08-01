package item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;

/**
 * A Water-Potion that restores a small amount of hit point on usage.
 *
 * <p>Can be used for crafting.
 */
public class ItemPotionWater extends contrib.item.concreteItem.ItemPotionWater {

  private static final int HEAL_AMOUNT = 4;

  static {
    Item.registerItem(ItemPotionWater.class);
  }

  /** Create a new Water-Potion. */
  public ItemPotionWater() {
    super();
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              e.fetch(HealthComponent.class)
                  .ifPresent(hc -> hc.receiveHit(new Damage(-HEAL_AMOUNT, DamageType.HEAL, null)));
            });
  }
}
