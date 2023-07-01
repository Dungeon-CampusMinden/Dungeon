package core.configuration;

import com.badlogic.gdx.Input;

import core.configuration.values.ConfigIntValue;

@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
    public static final ConfigKey<Integer> TOGGLE_FULLSCREEN =
            new ConfigKey<>(
                    new String[] {"graphics", "fullscreen"}, new ConfigIntValue(Input.Keys.F11));
}
