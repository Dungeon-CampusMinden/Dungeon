package configuration;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

public class Configuration {

    static {
        try {
            loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonValue configRoot;

    private static void loadConfiguration() throws IOException {
        File file = new File("./config.json");
        if(file.createNewFile()) { //Create file & load default config if not exists
            loadDefault();
            saveConfiguration();
        }

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        JsonReader jsonReader = new JsonReader();
        configRoot = jsonReader.parse(isr);
        isr.close();
        fis.close();

    }

    private static void saveConfiguration() {
    }

    public static <T> void set(ConfigKey<T> key, T value) {

    }

    public static <T> T get(ConfigKey<T> key) {
        String[] path = key.path;
        JsonValue node = configRoot;
        for(int i = 0; i < path.length; i ++) {
            String pathPart = path[i];
            if(!node.hasChild(pathPart)) {
                return key.defaultValue;
            }
            node = node.get(pathPart);
        }
        //TODO: Parse value & Return
        return null;
    }

    private static <T> void _set(ConfigKey<T> key, T value) {

        String[] path = key.path;
        JsonValue node = configRoot;
        for(int i = 0; i < path.length; i ++) {
            String pathPart = path[i];
            if(!node.hasChild(pathPart)) {
                node.addChild(pathPart, new JsonValue(JsonValue.ValueType.object));
            }
            node = node.get(pathPart);
        }
        node.set(value.toString());

        saveConfiguration();
    }

    private static void loadDefault() {

        //Get all static fields in KeyboardConfig with type ConfigKey
        Field[] fields = KeyboardConfig.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == ConfigKey.class && Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                try {
                    ConfigKey<?> key = (ConfigKey<?>) field.get(null);
                    //TODO: Set default value
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }



}
