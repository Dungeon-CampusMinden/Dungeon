package contrib.utils.components.item;

import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {

    private Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return new ItemData(Item.values()[rand.nextInt(Item.values().length)]);
    }
}
