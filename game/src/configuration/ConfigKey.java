package configuration;

import java.util.Arrays;

public class ConfigKey<DefaultType> {

    public final String[] path;
    public final DefaultType defaultValue;

    ConfigKey(String[] path, DefaultType defaultValue) {
        this.path = Arrays.stream(path).map(String::toLowerCase).toArray(String[]::new);
        this.defaultValue = defaultValue;
    }

    ConfigKey(String path, DefaultType defaultValue) {
        this.path = Arrays.stream(path.split("\\.")).map(String::toLowerCase).toArray(String[]::new);
        this.defaultValue = defaultValue;
    }

}
