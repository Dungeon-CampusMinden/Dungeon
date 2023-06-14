package contrib.utils.components.item;

import contrib.configuration.ItemConfig;

import core.utils.components.draw.Animation;

import java.util.List;

/**
 * It contains the {@link #category ItemCategory}, animations / textures for inside the hero
 * inventory ({@link #inventoryAnimation InventoryAnimation}) or in the world ({@link
 * #worldAnimation WorldAnimation}), as well as the {@link #name Name} of the Item and a {@link
 * #description}.
 */
public enum Item {
    WATER_BOTTLE(
            "Wasserflasche",
            "Eine Flasche gefüllt mit Wasser.",
            ItemCategory.BASIC,
            new Animation(List.of("item/water_bottle.png"), 1),
            new Animation(List.of("item/water_bottle.png"), 1)),
    MUSHROOM_RED(
            "Roter Pilz",
            "Ein ganz normaler, roter Pilz.",
            ItemCategory.BASIC,
            new Animation(List.of("item/mushroom_red.png"), 1),
            new Animation(List.of("item/mushroom_red.png"), 1)),
    MUSHROOM_BROWN(
            "Brauner Pilz",
            "Ein ganz normaler, brauner Pilz.",
            ItemCategory.BASIC,
            new Animation(List.of("item/mushroom_brown.png"), 1),
            new Animation(List.of("item/mushroom_brown.png"), 1)),
    STONE(
            "Stein",
            "Ein ganz normaler, grauer Stein.",
            ItemCategory.BASIC,
            new Animation(List.of("item/stone.png"), 1),
            new Animation(List.of("item/stone.png"), 1)),
    FLOWER_RED(
            "Rote Blume",
            "Eine rote Blume. Man erzählt sie hätte heilende Kräfte.",
            ItemCategory.BASIC,
            new Animation(List.of("item/flower_red.png"), 1),
            new Animation(List.of("item/flower_red.png"), 1)),
    LEATHER(
            "Leder",
            "Ein Stück Leder.",
            ItemCategory.BASIC,
            new Animation(List.of("item/leather.png"), 1),
            new Animation(List.of("item/leather.png"), 1)),
    STICK(
            "Stock",
            "Ein Stock.",
            ItemCategory.BASIC,
            new Animation(List.of("item/stick.png"), 1),
            new Animation(List.of("item/stick.png"), 1)),
    IRON_ORE(
            "Eisenerz",
            "Ein Stück Eisenerz. Daraus lassen sich sicherlich nützliche Dinge herstellen.",
            ItemCategory.BASIC,
            new Animation(List.of("item/iron_ore.png"), 1),
            new Animation(List.of("item/iron_ore.png"), 1)),
    HEALTH_POTION(
            "Heiltrank",
            "Ein Heiltrank. Er regeneriert 10 Lebenspunkte.",
            ItemCategory.ACTIVE,
            new Animation(List.of("item/health_potion.png"), 1),
            new Animation(List.of("item/health_potion.png"), 1));
    private String name, description;
    private Animation inventoryAnimation, worldAnimation;

    private ItemCategory category;

    Item(String name, String description, ItemCategory category) {
        this(
                name,
                description,
                category,
                new Animation(List.of(ItemConfig.TEXTURE.get()), 1),
                new Animation(List.of(ItemConfig.TEXTURE.get()), 1));
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

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Animation getInventoryAnimation() {
        return this.inventoryAnimation;
    }

    public Animation getWorldAnimation() {
        return this.worldAnimation;
    }
}
