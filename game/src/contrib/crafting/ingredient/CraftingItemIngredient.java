package contrib.crafting.ingredient;

import com.badlogic.gdx.utils.JsonValue;

import contrib.utils.components.item.Item;

public class CraftingItemIngredient extends CraftingIngredient {

    private Item item;
    private int count = 1;

    public CraftingItemIngredient() {
        super(Type.ITEM);
    }

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
