package contrib.utils.components.item;

import contrib.components.InventoryComponent;
import core.Entity;

public class DefaultUseCallback implements IOnUse{
    @Override
    public void onUse(Entity e, ItemData item) {
        e.getComponent(InventoryComponent.class)
            .ifPresent(
                component -> {
                    InventoryComponent invComp = (InventoryComponent) component;
                    invComp.removeItem(item);
                });
        System.out.printf("Item \"%s\" used by entity %d\n", item.getItemName(), e.id());
    }
}
