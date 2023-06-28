package contrib.crafting.result;

import com.badlogic.gdx.utils.JsonValue;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.Item;
import contrib.utils.components.item.ItemData;

import core.Entity;
import core.utils.components.MissingComponentException;

/** A crafting result that adds an item to the player's inventory. */
public class CraftingItemResult extends CraftingResult {

    private Item item;
    private int count = 1;

    /** Creates a new crafting item result. */
    public CraftingItemResult() {
        super(Type.ITEM);
    }

    /**
     * Creates a new crafting item result.
     *
     * @param item The item to add to the player's inventory.
     * @param count The number of items to add.
     */
    public CraftingItemResult(Item item, int count) {
        super(Type.ITEM);
        this.item = item;
        this.count = count;
    }

    /**
     * Returns the item which will be added to the entity's inventory.
     *
     * @return The item to add.
     */
    public Item item() {
        return this.item;
    }

    /**
     * Returns the number of items to add to the entity's inventory.
     *
     * @return The number of items to add.
     */
    public int count() {
        return this.count;
    }

    /**
     * Executes the crafting result. This will add the specified item to the inventory of the
     * entity.
     *
     * @param entity Entity to execute the result on.
     */
    @Override
    public void execute(Entity entity) {
        InventoryComponent ic =
                entity.fetch(InventoryComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, InventoryComponent.class));
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
