package contrib.item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.draw.SimpleIPath;

public class ItemPotionHealth extends Item {

    private static final int HEAL_AMOUNT = 50;

    public ItemPotionHealth() {
        super(
                "Health Potion",
                "A health potion. It heals you for " + HEAL_AMOUNT + " health points.",
                Animation.fromSingleImage(new SimpleIPath("items/potion/health_potion.png")));
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
