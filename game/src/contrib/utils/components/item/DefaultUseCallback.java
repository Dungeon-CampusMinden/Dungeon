package contrib.utils.components.item;

import contrib.components.InventoryComponent;

import core.Entity;

import java.util.function.BiConsumer;

public class DefaultUseCallback implements BiConsumer<Entity, ItemData> {
    @Override
    public void accept(Entity e, ItemData item) {
        e.fetch(InventoryComponent.class).ifPresent(component -> component.remove(item));
    }
}
