package configuration;

import com.badlogic.gdx.Input;
import configuration.values.ConfigIntValue;

@ConfigMap(path = { "keyboard" })
public class KeyboardConfig {

    public static final ConfigKey<Integer> MOVEMENT_UP = new ConfigKey<>(new String[] { "movement", "up" },
            new ConfigIntValue(Input.Keys.W));
    public static final ConfigKey<Integer> MOVEMENT_DOWN = new ConfigKey<>(new String[] { "movement", "down" },
            new ConfigIntValue(Input.Keys.S));
    public static final ConfigKey<Integer> MOVEMENT_LEFT = new ConfigKey<>(new String[] { "movement", "left" },
            new ConfigIntValue(Input.Keys.A));
    public static final ConfigKey<Integer> MOVEMENT_RIGHT = new ConfigKey<>(new String[] { "movement", "right" },
            new ConfigIntValue(Input.Keys.D));

    public static final ConfigKey<Integer> INVENTORY = new ConfigKey<>(new String[] { "inventory", "inventory" },
            new ConfigIntValue(Input.Keys.I));
    public static final ConfigKey<Integer> INVENTORY_FIRST = new ConfigKey<>(new String[] { "inventory", "first" },
            new ConfigIntValue(Input.Keys.NUM_1));

    public static final ConfigKey<Integer> INVENTORY_SECOND = new ConfigKey<>(new String[] { "inventory", "second" },
            new ConfigIntValue(Input.Keys.NUM_2));

    public static final ConfigKey<Integer> INVENTORY_THIRD = new ConfigKey<>(new String[] { "inventory", "third" },
            new ConfigIntValue(Input.Keys.NUM_3));

    public static final ConfigKey<Integer> INVENTORY_FOURTH = new ConfigKey<>(new String[] { "inventory", "fourth" },
            new ConfigIntValue(Input.Keys.NUM_4));

    public static final ConfigKey<Integer> INVENTORY_FIFTH = new ConfigKey<>(new String[] { "inventory", "fifth" },
            new ConfigIntValue(Input.Keys.NUM_5));

    public static final ConfigKey<Integer> INVENTORY_SIXTH = new ConfigKey<>(new String[] { "inventory", "sixth" },
            new ConfigIntValue(Input.Keys.NUM_6));

    public static final ConfigKey<Integer> INVENTORY_SEVENTH = new ConfigKey<>(new String[] { "inventory", "seventh" },
            new ConfigIntValue(Input.Keys.NUM_7));

    public static final ConfigKey<Integer> INVENTORY_EIGHTH = new ConfigKey<>(new String[] { "inventory", "eighth" },
            new ConfigIntValue(Input.Keys.NUM_8));

    public static final ConfigKey<Integer> INVENTORY_NINTH = new ConfigKey<>(new String[] { "inventory", "ninth" },
            new ConfigIntValue(Input.Keys.NUM_9));

    public static final ConfigKey<Integer> INVENTORY_REMOVE = new ConfigKey<>(new String[] { "inventory", "remove" },
            new ConfigIntValue(Input.Keys.L));

    public static final ConfigKey<Integer> INTERACT_WORLD = new ConfigKey<>(new String[] { "interact", "world" },
            new ConfigIntValue(Input.Keys.E));
    public static final ConfigKey<Integer> INTERACT_WORLD_X = new ConfigKey<>(new String[] { "interact", "world_x" },
            new ConfigIntValue(Input.Keys.X));
    public static final ConfigKey<Integer> FIRST_SKILL = new ConfigKey<>(new String[] { "skill", "first" },
            new ConfigIntValue(Input.Keys.Q));
    public static final ConfigKey<Integer> SECOND_SKILL = new ConfigKey<>(new String[] { "skill", "second" },
            new ConfigIntValue(Input.Keys.R));
    public static final ConfigKey<Integer> THIRD_SKILL = new ConfigKey<>(new String[] { "skill", "third" },
            new ConfigIntValue(Input.Keys.C));
    public static final ConfigKey<Integer> FOURTH_SKILL = new ConfigKey<>(new String[] { "skill", "fourth" },
            new ConfigIntValue(Input.Keys.V));
    public static final ConfigKey<Integer> FIFTH_SKILL = new ConfigKey<>(new String[] { "skill", "fifth" },
            new ConfigIntValue(Input.Keys.B));
    public static final ConfigKey<Integer> SIXTH_SKILL = new ConfigKey<>(new String[] { "skill", "sixth" },
            new ConfigIntValue(Input.Keys.N));
}
