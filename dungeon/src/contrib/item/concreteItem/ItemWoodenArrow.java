package contrib.item.concreteItem;

import contrib.components.AmmunitionComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public class ItemWoodenArrow extends Item {
  /** The default texture for all health potions. */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/weapon/wooden_arrow.png");

  public ItemWoodenArrow() {
    super("Wooden Arrow", "Ammunition for a Bow", Animation.fromSingleImage(DEFAULT_TEXTURE));
  }

  @Override
  public boolean collect(final Entity itemEntity, final Entity collector) {
    // the arrow collecting entity needs to have an ammoComp to collectAmmo
    if (collector.isPresent(AmmunitionComponent.class)) {
      collector.fetch(AmmunitionComponent.class).ifPresent(AmmunitionComponent::collectAmmo);
    } else {
      collector.add(new AmmunitionComponent());
      collector.fetch(AmmunitionComponent.class).ifPresent(AmmunitionComponent::collectAmmo);
    }

    return collector
        .fetch(InventoryComponent.class)
        .map(
            inventoryComponent -> {
              boolean hasArrow = inventoryComponent.hasItem(ItemWoodenArrow.class);

              // arrows consume only one inventory space
              if (!hasArrow) {
                if (!inventoryComponent.add(this)) {
                  return false;
                }
              }

              collector
                  .fetch(AmmunitionComponent.class)
                  .ifPresent(AmmunitionComponent::collectAmmo);
              Game.remove(itemEntity);
              return true;
            })
        .orElse(false);
  }

  @Override
  public void use(final Entity user) {
    Entity hero = Game.hero().orElseThrow();
    PositionComponent posc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    drop(posc.position());
    user.fetch(AmmunitionComponent.class).ifPresent(AmmunitionComponent::resetCurrentAmmunition);
    user.fetch(InventoryComponent.class).ifPresent(component -> component.remove(this));
  }
}
