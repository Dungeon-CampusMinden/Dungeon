package contrib.item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

/**
 * A Berry that restores hit point on usage.
 *
 * <p>Can be used for crafting.
 */
public class ItemResourceBerry extends Item {

  private static final int HEAL_AMOUNT = 5;

  /** Create a new Berry. */
  public ItemResourceBerry() {
    super("Berry", "A berry.", new Animation(new SimpleIPath("items/resource/berry.png")));
  }

  @Override
  public void use(final Entity e) {
    use(e, -1);
  }

  @Override
  public void use(final Entity e, int itemSlot) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              if (removeOneFromInventory(component, itemSlot)) {
                e.fetch(HealthComponent.class)
                    .ifPresent(
                        hc -> hc.receiveHit(new Damage(-HEAL_AMOUNT, DamageType.HEAL, null)));
              }
            });
  }
}
