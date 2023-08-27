package contrib.item;

import core.utils.components.draw.Animation;

/**
 * A default implementation of {@link Item}.
 *
 * <p>This class is used to create items that do not have any special functionality.
 */
public final class ItemDefault extends Item {

    private static final int DEFAULT_MAX_STACKSIZE = 16;

    /**
     * Create a new Item.
     *
     * @param displayName the display name of the item
     * @param description the description of the item
     * @param inventoryAnimation the inventory animation of the item
     * @param worldAnimation the world animation of the item
     */
    public ItemDefault(
            String displayName,
            String description,
            Animation inventoryAnimation,
            Animation worldAnimation) {
        super(displayName, description, inventoryAnimation, worldAnimation);
    }
}
