package ecs.items;

import graphic.Animation;

public class BasicItem extends Item {

    /** Fills the ITEM_REIGSTER with predefined Items */
    public static void FillRegister() {
        ITEM_REGISTER.add(
                new BasicItem(
                        ItemType.Basic,
                        new Animation(missingTexture, 1),
                        new Animation(missingTexture, 1),
                        "Buch",
                        "Ein sehr lehrreiches Buch."));
        ITEM_REGISTER.add(
                new BasicItem(
                        ItemType.Basic,
                        new Animation(missingTexture, 1),
                        new Animation(missingTexture, 1),
                        "Tuch",
                        "Ein sauberes Tuch.."));
        ITEM_REGISTER.add(
                new BasicItem(
                        ItemType.Basic,
                        new Animation(missingTexture, 1),
                        new Animation(missingTexture, 1),
                        "Namensschild",
                        "Ein Namensschild wo der Name nicht mehr lesbar ist.."));
    }

    static {
        FillRegister();
    }

    public BasicItem(
            ItemType itemType,
            Animation inventoryTexture,
            Animation worldTexture,
            String itemName,
            String description) {
        super(itemType, inventoryTexture, worldTexture, itemName, description);
    }
}
