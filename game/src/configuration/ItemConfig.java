package configuration;

import configuration.values.ConfigEnumValue;
import configuration.values.ConfigStringValue;
import ecs.items.ItemType;

/** The default ItemData values */
@ConfigMap(path = { "item" })
public class ItemConfig {
    /** The Description of the Default ItemData */
    public static final ConfigKey<String> DESCRIPTION = new ConfigKey<>(new String[] { "description" },
            new ConfigStringValue("Default Item"));

    public static final ConfigKey<String> BAG_DESCRIPTION = new ConfigKey<>(
            new String[] { "Bag takes three Items of the same type" },
            new ConfigStringValue("Bag takes three Items of the same type"));

    public static final ConfigKey<String> CAKE_DESCRIPTION = new ConfigKey<>(new String[] { "Healing for the Player" },
            new ConfigStringValue("Healing for the Player"));

    public static final ConfigKey<String> MONSTER_DESPAWN_DESCRIPTION = new ConfigKey<>(
            new String[] { "Despawns Monsters" }, new ConfigStringValue("Despawns Monsters"));

    public static final ConfigKey<String> SPEED_DESCRIPTION = new ConfigKey<>(
            new String[] { "Makes the Player faster" }, new ConfigStringValue("Makes the Player faster"));

    /** The Name of the Default ItemData */
    public static final ConfigKey<String> NAME = new ConfigKey<>(new String[] { "name" },
            new ConfigStringValue("Defaultname"));

    public static final ConfigKey<String> BAG_NAME = new ConfigKey<>(new String[] { "Bag" },
            new ConfigStringValue("Bag"));

    public static final ConfigKey<String> MONSTER_DESPAWN_NAME = new ConfigKey<>(new String[] { "Despawn" },
            new ConfigStringValue("Despawn"));

    public static final ConfigKey<String> SPEED_NAME = new ConfigKey<>(new String[] { "Speed" },
            new ConfigStringValue("Speed"));

    public static final ConfigKey<String> CAKE_NAME = new ConfigKey<>(new String[] { "Kuchen" },
            new ConfigStringValue("Kuchen"));

    /** The Type of the Default ItemData */
    public static final ConfigKey<ItemType> TYPE = new ConfigKey<>(new String[] { "type" },
            new ConfigEnumValue<>(ItemType.Basic));

    public static final ConfigKey<ItemType> POTION_TYPE = new ConfigKey<>(new String[] { "Potion" },
            new ConfigEnumValue<>(ItemType.Potion));

    public static final ConfigKey<ItemType> BAG_TYPE = new ConfigKey<>(new String[] { "Bag" },
            new ConfigEnumValue<>(ItemType.Bag));

    public static final ConfigKey<ItemType> FOOD_TYPE = new ConfigKey<>(new String[] { "Food" },
            new ConfigEnumValue<>(ItemType.Food));

    /**
     * The texturepath of the Default ItemData will be used for world and Inventory
     */
    public static final ConfigKey<String> TEXTURE = new ConfigKey<>(
            new String[] { "texture" },
            new ConfigStringValue("animation/missingTexture.png"));

    public static final ConfigKey<String> BAG_TEXTURE = new ConfigKey<>(
            new String[] { "bagTexture" },
            new ConfigStringValue("items/bag.png"));

    public static final ConfigKey<String> MONSTER_DESPAWN_TEXTURE = new ConfigKey<>(
            new String[] { "flowerTexture" },
            new ConfigStringValue("items/despawn.png"));

    public static final ConfigKey<String> CAKE_TEXTURE = new ConfigKey<>(
            new String[] { "healthTexture" },
            new ConfigStringValue("items/flower.png"));

    public static final ConfigKey<String> SPEED_TEXTURE = new ConfigKey<>(
            new String[] { "speedTexture" },
            new ConfigStringValue("items/speed.png"));
}
