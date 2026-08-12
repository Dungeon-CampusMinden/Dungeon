package feature.inventory.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.HealthComponent;
import feature.components.InventoryComponent;
import feature.health.Damage;
import feature.health.DamageType;
import feature.inventory.Item;

/** A Berry that restores hit point on usage. */
public class ItemResourceBerry extends Item {

  private static final int HEAL_AMOUNT = 5;

  /** Create a new Berry. */
  public ItemResourceBerry() {
    super("Berry", "A berry.", new Animation(new SimpleIPath("items/resource/berry.png")));
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
