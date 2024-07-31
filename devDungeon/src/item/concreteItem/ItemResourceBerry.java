package item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;

/**
 * A Berry that restores hit point on usage.
 *
 * <p>Can be used for crafting.
 */
public class ItemResourceBerry extends contrib.item.concreteItem.ItemResourceBerry {

  private static final int DamageAmount = 2;

  static {
    Item.registerItem(ItemResourceBerry.class);
  }

  /** Create a new Berry. */
  public ItemResourceBerry() {
    super();
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              e.fetch(HealthComponent.class)
                  .ifPresent(
                      hc -> {
                        if (Item.RANDOM.nextBoolean()) {
                          hc.receiveHit(new Damage(-DamageAmount, DamageType.HEAL, null));
                        } else {
                          hc.receiveHit(new Damage(DamageAmount, DamageType.POISON, null));
                        }
                      });
            });
  }
}
