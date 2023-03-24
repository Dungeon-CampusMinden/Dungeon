package configuration;

import configuration.values.ConfigEnumValue;
import configuration.values.ConfigStringValue;
import ecs.items.ItemType;

@ConfigMap(path = {"item"})
public class ItemConfig {
    public static final ConfigKey<String> DESCRIPTION =
            new ConfigKey<>(new String[] {"description"}, new ConfigStringValue("Default Item"));
    public static final ConfigKey<String> NAME =
            new ConfigKey<>(new String[] {"name"}, new ConfigStringValue("Defaultname"));
    public static final ConfigKey<ItemType> TYPE =
            new ConfigKey<>(new String[] {"type"}, new ConfigEnumValue<>(ItemType.Basic));
    public static final ConfigKey<String> TEXTURE =
            new ConfigKey<>(
                    new String[] {"texture"},
                    new ConfigStringValue("animation/missingTexture.png"));
}
