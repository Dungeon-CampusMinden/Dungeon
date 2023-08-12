package contrib.utils.components.item;

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
    SWORD_FIRE(
            "Fire Sword",
            "A sword that is on fire. Handle with care!",
            ItemCategory.WEAPON,
            "items/weapon/fire_sword.png"),
    SWORD_ICE(
            "Ice Sword",
            "A sword that is made of ice. Be carefull, it's really cold!",
            ItemCategory.WEAPON,
            "items/weapon/ice_sword.png"),
    SWORD_LEGENDARY(
            "Legendary Sword",
            "A legendary sword. It's said that it was forged by the gods.",
            ItemCategory.WEAPON,
            "items/weapon/legendary_sword.png"),
    SWORD_LIGHTNING(
            "Lightning Sword",
            "A sword that is made of pure lightning. It's said that it was forged by the gods.",
            ItemCategory.WEAPON,
            "items/weapon/lightning_sword.png"),
    SWORD_RAINBOW(
            "Rainbow Sword",
            "A sword that is made of a rainbow. Maybe it leads you to a pot full of gold?",
            ItemCategory.WEAPON,
            "items/weapon/rainbow_sword.png"),
    SWORD_SNAKE(
            "Snake Sword",
            "A sword that is made of a snake.",
            ItemCategory.WEAPON,
            "items/weapon/snake_sword.png"),

    SHIELD_KNIGHT(
            "Knights Shield",
            "A shield that is used by knights.",
            ItemCategory.TOOL,
            "items/shield/knight_shield.png"),
    SHIELD_VIKING(
            "Vikings Shield",
            "A shield that is used by vikings.",
            ItemCategory.TOOL,
            "items/shield/viking_shield.png"),

    RING_BEHOLDER(
            "Beholder Ring",
            "A ring that is used by beholders.",
            ItemCategory.TOOL,
            "items/ring/beholder_ring.png"),
    RING_HEART(
            "Heart Ring",
            "A ring with a heart on it.",
            ItemCategory.TOOL,
            "items/ring/heart_ring.png"),

    RESOURCE_BASALT(
            "Basalt", "Its just basalt.", ItemCategory.RESOURCE, "items/resource/basalt.png"),
    RESOURCE_BEER("Beer", "A mug of beer.", ItemCategory.RESOURCE, "items/resource/beer.png"),
    RESOURCE_BERRY("Berry", "A berry.", ItemCategory.RESOURCE, "items/resource/berry.png"),
    RESOURCE_BLOBS("Blobs", "Blobs.", ItemCategory.RESOURCE, "items/resource/blobs.png"),
    RESOURCE_BONE("Bone", "A bone.", ItemCategory.RESOURCE, "items/resource/bone.png"),
    RESOURCE_CHEESE(
            "Cheese", "A piece of cheese.", ItemCategory.RESOURCE, "items/resource/cheese.png"),
    RESOURCE_CLOVERLEAF(
            "Cloverleaf",
            "A cloverleaf. May it bring you luck!",
            ItemCategory.RESOURCE,
            "items/resource/cloverleaf.png"),
    RESOURCE_COFFEE(
            "Coffee",
            "A cup of coffee. The morning is only bearable with it.",
            ItemCategory.RESOURCE,
            "items/resource/coffee.png"),
    RESOURCE_DONUT(
            "Donut",
            "A donut. Don't let a cop see it!",
            ItemCategory.RESOURCE,
            "items/resource/donut.png"),
    RESOURCE_DRAGON_TOOTH(
            "Dragon Tooth",
            "A tooth of a dragon. Someone really brave must have taken it from the dragon.",
            ItemCategory.RESOURCE,
            "items/resource/dragon_tooth.png"),
    RESOURCE_DRAGON_WING(
            "Dragon Wing",
            "A wing of a dragon. Someone really brave must have taken it from the dragon.",
            ItemCategory.RESOURCE,
            "items/resource/dragon_wing.png"),
    RESOURCE_EGG(
            "Egg",
            "An egg. What was there before? The chicken or the egg?",
            ItemCategory.RESOURCE,
            "items/resource/egg.png"),
    RESOURCE_EMERALD(
            "Emerald",
            "An emerald. It's said that it has magical powers.",
            ItemCategory.RESOURCE,
            "items/resource/emerald.png"),
    RESOURCE_GOLD("Gold", "A piece of gold.", ItemCategory.RESOURCE, "items/resource/gold.png"),
    RESOURCE_LEAF("Leaf", "A leaf.", ItemCategory.RESOURCE, "items/resource/leaf.png"),
    RESOURCE_LEATHER(
            "Leather",
            "A piece of leather, probably torn of a cow.",
            ItemCategory.RESOURCE,
            "items/resource/leather.png"),
    RESOURCE_PRETZEL(
            "Pretzel",
            "A pretzel is a typically german type of baked pastry made from dough that is shaped into a knot.",
            ItemCategory.RESOURCE,
            "items/resource/pretzel.png"),
    RESOURCE_SAPHIRE(
            "Saphire",
            "A saphire. It's said that it has magical powers.",
            ItemCategory.RESOURCE,
            "items/resource/saphire.png"),
    RESOURCE_SKULL(
            "Skull",
            "A skull. I wonder who it used to be?",
            ItemCategory.RESOURCE,
            "items/resource/skull.png"),
    RESOURCE_STEEL("Steel", "A piece of steel.", ItemCategory.RESOURCE, "items/resource/steel.png"),
    RESOURCE_SULPHUR(
            "Sulphur", "Some sulphur.", ItemCategory.RESOURCE, "items/resource/sulphur.png"),
    RESOURCE_TENTACLE(
            "Tentacle", "A tentacle.", ItemCategory.RESOURCE, "items/resource/tentacle.png"),
    RESOURCE_TOADSTOOL(
            "Toadstool",
            "A toadstool. But don't eat it!",
            ItemCategory.RESOURCE,
            "items/resource/toadstool.png"),
    RESOURCE_TOPAS(
            "Topas",
            "A topas. It's said that it has magical powers.",
            ItemCategory.RESOURCE,
            "items/resource/topas.png"),
    RESOURCE_WOOD("Wood", "A piece of wood.", ItemCategory.RESOURCE, "items/resource/wood.png"),
    RESOURCE_FLOWER_RED(
            "Red Flower", "A red flower.", ItemCategory.RESOURCE, "items/resource/flower_red.png"),
    RESOURCE_IRON_ORE(
            "Iron Ore",
            "A piece of iron ore.",
            ItemCategory.RESOURCE,
            "items/resource/iron_ore.png"),
    RESOURCE_MUSHROOM_BROWN(
            "Brown Mushroom",
            "A brown mushroom.",
            ItemCategory.RESOURCE,
            "items/resource/mushroom_brown.png"),
    RESOURCE_MUSHROOM_RED(
            "Red Mushroom",
            "A red mushroom.",
            ItemCategory.RESOURCE,
            "items/resource/mushroom_red.png"),
    RESOURCE_STICK("Stick", "A stick.", ItemCategory.RESOURCE, "items/resource/stick.png"),
    RESOURCE_STONE("Stone", "A stone.", ItemCategory.RESOURCE, "items/resource/stone.png"),

    POTION_ANTIDOTE(
            "Antidote",
            "An antidote. It cures poison.",
            ItemCategory.FOOD,
            "items/potion/antidote_potion.png"),
    POTION_HEALTH(
            "Health Potion",
            "A health potion. It heals you several health points.",
            ItemCategory.FOOD,
            "items/potion/health_potion.png"),
    POTION_MANA(
            "Mana Potion",
            "A mana potion. It restores your mana.",
            ItemCategory.FOOD,
            "items/potion/mana_potion.png"),
    BOTTLE_WATER(
            "Water Bottle",
            "A bottle filled with water.",
            ItemCategory.FOOD,
            "items/potion/water_bottle.png"),

    NECKLACE_GOLDEN(
            "Golden Necklace",
            "A golden necklace.",
            ItemCategory.WEARABLE,
            "items/necklace/golden_necklace.png"),
    NECKLACE_MAGIC(
            "Magic Necklace",
            "A magic necklace. It's said that it was forged by the gods.",
            ItemCategory.WEARABLE,
            "items/necklace/magic_necklace.png"),

    KEY_BLUE("Blue Key", "A blue key.", ItemCategory.TOOL, "items/key/blue_key.png"),
    KEY_GOLD("Golden Key", "A golden key.", ItemCategory.TOOL, "items/key/gold_key.png"),
    KEY_RED("Red Key", "A red key.", ItemCategory.TOOL, "items/key/red_key.png"),

    GLOVES_FIRE(
            "Fire Gloves",
            "A pair of gloves. But look it is on fire!?",
            ItemCategory.TOOL,
            "items/gloves/fire_gloves.png"),
    GLOVES_STEEL(
            "Steel Gloves",
            "A pair of gloves. Made of steel.",
            ItemCategory.TOOL,
            "items/gloves/steel_gloves.png"),

    BOOK_RED("Rotes Buch", "A red book.", ItemCategory.TOOL, "items/book/red_book.png"),
    BOOK_SPELL(
            "Spell Book",
            "A spell book. It contains many interesting sentences.",
            ItemCategory.TOOL,
            "items/book/spell_book.png"),
    LETTER(
            "Letter",
            "A letter. I wonder what it says.",
            ItemCategory.OTHER,
            "items/book/letter.png"),
    MAGIC_SCROLL(
            "Magic Roll",
            "A magic roll. It's said that it was forged by the gods.",
            ItemCategory.TOOL,
            "items/book/magic_scroll.png"),
    WISDOM_SCROLL(
            "Wisdom Roll",
            "A wisdom roll. Some of it could be very informative.",
            ItemCategory.TOOL,
            "items/book/wisdom_scroll.png"),

    ARMOR_BODY_BEAR(
            "Bear Body Armor",
            "A body armor made of bear fur.",
            ItemCategory.WEARABLE,
            "items/armor/body/bear_armor.png"),
    ARMOR_BODY_CHAINMAIL(
            "Chainmail Body Armor",
            "A chainmail body armor.",
            ItemCategory.WEARABLE,
            "items/armor/body/chainmail_armor.png"),
    ARMOR_BODY_CHICKEN(
            "Chicken Body Armor",
            "A body armor made of chicken feathers.",
            ItemCategory.WEARABLE,
            "items/armor/body/chicken_armor.png"),
    ARMOR_BODY_PINK(
            "Pink Body Armor",
            "A pink body armor.",
            ItemCategory.WEARABLE,
            "items/armor/body/pink_armor.png"),
    ARMOR_BODY_PLATE(
            "Plate Body Armor",
            "A plate body armor.",
            ItemCategory.WEARABLE,
            "items/armor/body/plate_armor.png"),
    ARMOR_BODY_UNDEAD(
            "Undead Body Armor",
            "A body armor made of undead skin.",
            ItemCategory.WEARABLE,
            "items/armor/body/undead_armor.png"),

    ARMOR_HELMET_BEAR(
            "Bear Helmet",
            "A helmet made of bear fur.",
            ItemCategory.WEARABLE,
            "items/armor/helmet/bear_helmet.png"),
    ARMOR_HELMET_CHAINMAIL(
            "Chainmail Helmet",
            "A chainmail helmet.",
            ItemCategory.WEARABLE,
            "items/armor/helmet/chainmail_helmet.png"),
    ARMOR_HELMET_CHICKEN(
            "Chicken Helmet",
            "A helmet made of chicken feathers.",
            ItemCategory.WEARABLE,
            "items/armor/helmet/chicken_helmet.png"),
    ARMOR_HELMET_PINK(
            "Pink Helmet",
            "A pink helmet.",
            ItemCategory.WEARABLE,
            "items/armor/helmet/pink_helmet.png"),
    ARMOR_HELMET_PLATE(
            "Plate Helmet",
            "A plate helmet.",
            ItemCategory.WEARABLE,
            "items/armor/helmet/plate_helmet.png"),
    ARMOR_HELMET_UNDEAD(
            "Undead Helmet",
            "A helmet made of undead skin.",
            ItemCategory.WEARABLE,
            "items/armor/helmet/undead_helmet.png"),

    ARMOR_PANTS_BEAR(
            "Bear Pants",
            "Pants made of bear fur.",
            ItemCategory.WEARABLE,
            "items/armor/pants/bear_pants.png"),
    ARMOR_PANTS_CHAINMAIL(
            "Chainmail Pants",
            "Chainmail pants.",
            ItemCategory.WEARABLE,
            "items/armor/pants/chainmail_pants.png"),
    ARMOR_PANTS_CHICKEN(
            "Chicken Pants",
            "Pants made of chicken feathers.",
            ItemCategory.WEARABLE,
            "items/armor/pants/chicken_pants.png"),
    ARMOR_PANTS_PINK(
            "Pink Pants", "Pink pants.", ItemCategory.WEARABLE, "items/armor/pants/pink_pants.png"),
    ARMOR_PANTS_PLATE(
            "Plate Pants",
            "Plate pants.",
            ItemCategory.WEARABLE,
            "items/armor/pants/plate_pants.png"),
    ARMOR_PANTS_UNDEAD(
            "Undead Pants",
            "Pants made of undead skin.",
            ItemCategory.WEARABLE,
            "items/armor/pants/undead_pants.png"),

    DEFAULT_ITEM("Default Item", "Default Item", ItemCategory.OTHER);
    private String name, description;
    private Animation inventoryAnimation, worldAnimation;

    private ItemCategory category;

    Item(String name, String description, ItemCategory category) {
        this(
                name,
                description,
                category,
                new Animation("animation/missing_texture.png"),
                new Animation("animation/missing_texture.png"));
    }

    Item(String name, String description, ItemCategory category, Animation animation) {
        this(name, description, category, animation, animation);
    }

    Item(String name, String description, ItemCategory category, String texturePath) {
        this(name, description, category, new Animation(texturePath), new Animation(texturePath));
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
