package feature.inventory.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.HealthComponent;
import feature.components.InventoryComponent;
import feature.health.Damage;
import feature.health.DamageType;
import feature.inventory.Item;

/** A Water-Potion that restores a small amount of hit point on usage. */
public class ItemPotionWater extends Item {

  private static final int HEAL_AMOUNT = 5;

  /** Create a new Water-Potion. */
  public ItemPotionWater() {
    super(
        "Bottle of Water",
        "A bottle of water. It's not very useful except for hydration. It heals you for "
            + HEAL_AMOUNT
            + " health points.",
        new Animation(new SimpleIPath("items/potion/water_bottle.png")));
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.removeOne(this);
              e.fetch(HealthComponent.class)
                  .ifPresent(hc -> hc.receiveHit(new Damage(-HEAL_AMOUNT, DamageType.HEAL, null)));
            });
  }
}
