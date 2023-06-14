package contrib.crafting.result;

import com.badlogic.gdx.utils.JsonValue;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.Item;
import contrib.utils.components.item.ItemData;

import core.Entity;
import core.utils.components.MissingComponentException;

public class CraftingItemResult extends CraftingResult {

    private Item item;
    private int count = 1;

    public CraftingItemResult() {
        super(Type.ITEM);
    }

    public CraftingItemResult(Item item, int count) {
        super(Type.ITEM);
        this.item = item;
        this.count = count;
    }

    @Override
    public void execute(Entity entity) {
        InventoryComponent ic =
                (InventoryComponent)
                        entity.getComponent(InventoryComponent.class)
                                .orElseThrow(
                                        () ->
                                                new MissingComponentException(
                                                        "Could not execute crafting result"));
        for (int i = 0; i < this.count; i++) {
            ic.addItem(new ItemData(this.item));
        }
    }

    @Override
    public void parse(JsonValue value) {
        this.item = Item.valueOf(value.getString("id").toUpperCase());
        this.count = value.getInt("count", 1);
    }
}
