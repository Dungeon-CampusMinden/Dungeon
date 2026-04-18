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
 * A Mushroom that removes a small amount of hit point on usage.
 *
 * <p>Can be used for crafting.
 */
public class ItemResourceMushroomRed extends Item {

  private static final int DAMAGE_AMOUNT = 20;

  /** Create a new Mushroom. */
  public ItemResourceMushroomRed() {
    super(
        "Red Mushroom",
        "A red mushroom.",
        new Animation(new SimpleIPath("items/resource/mushroom_red.png")));
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
                        hc -> hc.receiveHit(new Damage(DAMAGE_AMOUNT, DamageType.POISON, null)));
              }
            });
  }
}
