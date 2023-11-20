package contrib.item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.utils.components.draw.Animation;

public class ItemResourceMushroomRed extends Item {

    private static final int DAMAGE_AMOUNT = 20;

    public ItemResourceMushroomRed() {
        super(
                "Red Mushroom",
                "A red mushroom.",
                Animation.fromSingleImage("items/resource/mushroom_red.png"));
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
                                                                    DAMAGE_AMOUNT,
                                                                    DamageType.POISON,
                                                                    null)));
                        });
    }
}
