package feature.inventory.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.HealthComponent;
import feature.components.InventoryComponent;
import feature.health.Damage;
import feature.health.DamageType;
import feature.inventory.Item;

/** A Mushroom that removes a small amount of hit point on usage. */
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
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.removeOne(this);
              e.fetch(HealthComponent.class)
                  .ifPresent(
                      hc -> hc.receiveHit(new Damage(DAMAGE_AMOUNT, DamageType.POISON, null)));
            });
  }
}
