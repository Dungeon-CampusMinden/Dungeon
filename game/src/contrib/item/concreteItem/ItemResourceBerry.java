package contrib.item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;

public class ItemResourceBerry extends Item {

    private static final int HEAL_AMOUNT = 5;

    public ItemResourceBerry() {
        super(
                "Berry",
                "A berry.",
                Animation.fromSingleImage(new SimpleIPath("items/resource/berry.png")));
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
