package contrib.configuration;

import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigStringValue;

/** The default ItemData values */
@ConfigMap(path = {"item"})
public class ItemConfig {

    /** The texturepath of the Default ItemData will be used for world and Inventory */
    public static final ConfigKey<String> TEXTURE =
            new ConfigKey<>(
                    new String[] {"texture"},
                    new ConfigStringValue("animation/missingTexture.png"));

    public static final ConfigKey<String> DEFAULT_ITEM =
            new ConfigKey<>(new String[] {"default", "item"}, new ConfigStringValue("STONE"));
}
