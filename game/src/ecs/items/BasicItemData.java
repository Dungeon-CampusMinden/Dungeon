package ecs.items;

import graphic.Animation;

public class BasicItemData extends ItemData {

    /** Fills the ITEM_REIGSTER with predefined Items */
    public static void FillRegister() {
        ITEM_DATA_REGISTER.add(
                new BasicItemData(
                        ItemType.Basic,
                        new Animation(missingTexture, 1),
                        new Animation(missingTexture, 1),
                        "Buch",
                        "Ein sehr lehrreiches Buch."));
        ITEM_DATA_REGISTER.add(
                new BasicItemData(
                        ItemType.Basic,
                        new Animation(missingTexture, 1),
                        new Animation(missingTexture, 1),
                        "Tuch",
                        "Ein sauberes Tuch.."));
        ITEM_DATA_REGISTER.add(
                new BasicItemData(
                        ItemType.Basic,
                        new Animation(missingTexture, 1),
                        new Animation(missingTexture, 1),
                        "Namensschild",
                        "Ein Namensschild wo der Name nicht mehr lesbar ist.."));
    }

    static {
        FillRegister();
    }

    public BasicItemData(
            ItemType itemType,
            Animation inventoryTexture,
            Animation worldTexture,
            String itemName,
            String description) {
        super(itemType, inventoryTexture, worldTexture, itemName, description);
    }
}
