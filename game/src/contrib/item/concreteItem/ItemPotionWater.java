package contrib.item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.utils.components.draw.Animation;

public class ItemPotionWater extends Item {

    private static final int HEAL_AMOUNT = 5;

    public ItemPotionWater() {
        super(
                "Bottle of Water",
                "A bottle of water. It's not very useful except for hydration. It heals you for "
                        + HEAL_AMOUNT
                        + " health points.",
                Animation.of("items/potion/water_bottle.png"));
    }

    @Override
    public void use(Entity e) {
        e.fetch(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            component.remove(this);
                            e.fetch(HealthComponent.class)
                                    .ifPresent(
                                            hc ->
                                                    hc.receiveHit(
                                                            new Damage(
                                                                    -HEAL_AMOUNT,
                                                                    DamageType.HEAL,
                                                                    null)));
                        });
    }
}
