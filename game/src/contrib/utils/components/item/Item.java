package contrib.utils.components.item;

import contrib.configuration.ItemConfig;

import core.utils.components.draw.Animation;

/**
 * This is an enum that represents all items in the game.
 *
 * <p>It contains the {@link #category ItemCategory}, animations / textures for inside the hero
 * inventory ({@link #inventoryAnimation InventoryAnimation}) or in the world ({@link
 * #worldAnimation WorldAnimation}), as well as the {@link #name Name} of the Item and a {@link
 * #description}.
 */
public enum Item {
    WATER_BOTTLE(
            "Wasserflasche",
            "Eine Flasche gefüllt mit Wasser.",
            ItemCategory.BASIC,
            new Animation("item/water_bottle.png"),
            new Animation("item/water_bottle.png")),
    MUSHROOM_RED(
            "Roter Pilz",
            "Ein ganz normaler, roter Pilz.",
            ItemCategory.BASIC,
            new Animation("item/mushroom_red.png"),
            new Animation("item/mushroom_red.png")),
    MUSHROOM_BROWN(
            "Brauner Pilz",
            "Ein ganz normaler, brauner Pilz.",
            ItemCategory.BASIC,
            new Animation("item/mushroom_brown.png"),
            new Animation("item/mushroom_brown.png")),
    STONE(
            "Stein",
            "Ein ganz normaler, grauer Stein.",
            ItemCategory.BASIC,
            new Animation("item/stone.png"),
            new Animation("item/stone.png")),
    FLOWER_RED(
            "Rote Blume",
            "Eine rote Blume. Man erzählt sie hätte heilende Kräfte.",
            ItemCategory.BASIC,
            new Animation("item/flower_red.png"),
            new Animation("item/flower_red.png")),
    LEATHER(
            "Leder",
            "Ein Stück Leder.",
            ItemCategory.BASIC,
            new Animation("item/leather.png"),
            new Animation("item/leather.png")),
    STICK(
            "Stock",
            "Ein Stock.",
            ItemCategory.BASIC,
            new Animation("item/stick.png"),
            new Animation("item/stick.png")),
    IRON_ORE(
            "Eisenerz",
            "Ein Stück Eisenerz. Daraus lassen sich sicherlich nützliche Dinge herstellen.",
            ItemCategory.BASIC,
            new Animation("item/iron_ore.png"),
            new Animation("item/iron_ore.png")),
    HEALTH_POTION(
            "Heiltrank",
            "Ein Heiltrank. Er regeneriert 10 Lebenspunkte.",
            ItemCategory.ACTIVE,
            new Animation("item/health_potion.png"),
            new Animation("item/health_potion.png"));
    private String name, description;
    private Animation inventoryAnimation, worldAnimation;

    private ItemCategory category;

    Item(String name, String description, ItemCategory category) {
        this(
                name,
                description,
                category,
                new Animation(ItemConfig.TEXTURE.value()),
                new Animation(ItemConfig.TEXTURE.value()));
    }

    Item(
            String name,
            String description,
            ItemCategory category,
            Animation inventoryAnimation,
            Animation worldAnimation) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.inventoryAnimation = inventoryAnimation;
        this.worldAnimation = worldAnimation;
    }

    public String displayName() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public Animation inventoryAnimation() {
        return this.inventoryAnimation;
    }

    public Animation worldAnimation() {
        return this.worldAnimation;
    }
}
