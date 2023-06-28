package contrib.crafting.ingredient;

import com.badlogic.gdx.utils.JsonValue;

import contrib.utils.components.item.Item;

/**
 * Represents an item ingredient in a crafting recipe.
 *
 * <p>The ingredient is defined by the item and the amount of items required.
 */
public class CraftingItemIngredient extends CraftingIngredient {

    private Item item;
    private int count = 1;

    /** Create a new item ingredient. */
    public CraftingItemIngredient() {
        super(Type.ITEM);
    }

    /**
     * Create a new item ingredient.
     *
     * @param item The item.
     * @param count The amount of items required.
     */
    public CraftingItemIngredient(Item item, int count) {
        super(Type.ITEM);
        this.item = item;
        this.count = count;
    }

    @Override
    public boolean match(CraftingIngredient input) {
        if (!(input instanceof CraftingItemIngredient inputItem)) {
            return false;
        }
        if (inputItem.item != this.item) {
            return false;
        }
        if (inputItem.count < this.count) {
            return false;
        }
        return true;
    }

    @Override
    public void parse(JsonValue value) {
        this.item = Item.valueOf(value.getString("id").toUpperCase());
        this.count = value.getInt("count", 1);
    }
}
