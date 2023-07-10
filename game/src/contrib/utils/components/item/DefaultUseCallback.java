package contrib.utils.components.item;

import contrib.components.InventoryComponent;

import core.Entity;

import java.util.function.BiConsumer;

public class DefaultUseCallback implements BiConsumer<Entity, ItemData> {
    @Override
    public void accept(Entity e, ItemData item) {
        e.fetch(InventoryComponent.class).ifPresent(component -> component.removeItem(item));
        System.out.printf("Item \"%s\" used by entity %d\n", item.itemName(), e.id());
    }
}
