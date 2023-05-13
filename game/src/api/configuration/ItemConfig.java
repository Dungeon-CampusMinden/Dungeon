package api.configuration;

import api.configuration.ConfigKey;
import api.configuration.ConfigMap;
import api.configuration.values.ConfigEnumValue;
import api.configuration.values.ConfigStringValue;
import api.utils.componentUtils.itemComponent.ItemType;

/** The default ItemData values */
@ConfigMap(path = {"item"})
public class ItemConfig {
    /** The Description of the Default ItemData */
    public static final ConfigKey<String> DESCRIPTION =
            new ConfigKey<>(new String[] {"description"}, new ConfigStringValue("Default Item"));

    /** The Name of the Default ItemData */
    public static final ConfigKey<String> NAME =
            new ConfigKey<>(new String[] {"name"}, new ConfigStringValue("Defaultname"));

    /** The Type of the Default ItemData */
    public static final ConfigKey<ItemType> TYPE =
            new ConfigKey<>(new String[] {"type"}, new ConfigEnumValue<>(ItemType.Basic));

    /** The texturepath of the Default ItemData will be used for world and Inventory */
    public static final ConfigKey<String> TEXTURE =
            new ConfigKey<>(
                    new String[] {"texture"},
                    new ConfigStringValue("animation/missingTexture.png"));
}
