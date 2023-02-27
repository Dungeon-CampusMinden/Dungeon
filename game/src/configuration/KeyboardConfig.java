package configuration;

public class KeyboardConfig {

    public static final ConfigKey<String> MOVEMENT_UP = new ConfigKey<>(new String[]{"keyboard", "movement", "up"}, "w");
    public static final ConfigKey<String> MOVEMENT_DOWN = new ConfigKey<>(new String[]{"keyboard", "movement", "down"}, "s");
    public static final ConfigKey<String> MOVEMENT_LEFT = new ConfigKey<>(new String[]{"keyboard", "movement", "left"}, "a");
    public static final ConfigKey<String> MOVEMENT_RIGHT = new ConfigKey<>(new String[]{"keyboard", "movement", "right"}, "d");
    public static final ConfigKey<String> INVENTORY_OPEN = new ConfigKey<>(new String[]{"keyboard", "inventory", "open"}, "i");
    public static final ConfigKey<String> INTERACT_WORLD = new ConfigKey<>(new String[]{"keyboard", "interact", "world"}, "e");

}
