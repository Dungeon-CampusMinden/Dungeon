package item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;

/**
 * A Mushroom that removes a small amount of hit point on usage.
 *
 * <p>Can be used for crafting.
 */
public class ItemResourceMushroomRed extends contrib.item.concreteItem.ItemResourceMushroomRed {

  static {
    Item.registerItem(ItemResourceMushroomRed.class);
  }

  /** Create a new Mushroom. */
  public ItemResourceMushroomRed() {
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
                      hc ->
                          hc.receiveHit(
                              new Damage(hc.currentHealthpoints() - 1, DamageType.POISON, null)));
            });
  }
}
