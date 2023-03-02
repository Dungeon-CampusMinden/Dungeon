package configuration;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class Configuration {

    private static final Class<?>[] CONFIG_CLASSES = new Class<?>[] {KeyboardConfig.class};
    private static boolean fieldsLoaded = false;
    private static JsonValue configRoot;
    private static final JsonValue.PrettyPrintSettings prettyPrintSettings =
            new JsonValue.PrettyPrintSettings();

    private static String currentConfigFilePath = "./config.json";

    static {
        prettyPrintSettings.outputType = JsonWriter.OutputType.json;
        prettyPrintSettings.wrapNumericArrays = true;
        prettyPrintSettings.singleLineColumns = 0;
        prepareFields();
    }

    /**
     * Loads the default configuration (dungeon_config.json in work directory)
     *
     * @throws IOException
     */
    public static void loadConfiguration() throws IOException {
        loadConfiguration("./dungeon_config.json");
    }

    /**
     * Loads the configuration from the given path
     *
     * @param path Path to the configuration file
     * @throws IOException
     */
    public static void loadConfiguration(String path) throws IOException {
        currentConfigFilePath = path;
        File file = new File(path);
        if (file.createNewFile()) { // Create file & load default config if not exists
            loadDefault();
            saveConfiguration();
            return;
        }

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        JsonReader jsonReader = new JsonReader();
        configRoot = jsonReader.parse(isr);
        isr.close();
        fis.close();

        if (configRoot == null) {
            loadDefault();
            saveConfiguration();
        }

        Stream.of(findConfigFields())
                .map(
                        field -> {
                            try {
                                return field.get(null);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .forEach(
                        obj -> {
                            ConfigKey<?> configKey = (ConfigKey<?>) obj;
                            JsonValue node = findOrCreate(configKey.path);
                            configKey.value.deserialize(node.asString());
                        });
    }

    /** Save the current configuration to the file */
    private static void saveConfiguration() {
        try {
            File file = new File(currentConfigFilePath);
            FileOutputStream fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            osw.write(configRoot.prettyPrint(prettyPrintSettings));
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds JsonNode for the given path. If the node does not exist, it will be created.
     *
     * @param path Path to the node
     * @return JsonNode for the given path
     */
    private static JsonValue findOrCreate(String[] path) {
        JsonValue node = configRoot;
        for (String pathPart : path) {
            if (!node.hasChild(pathPart)) {
                node.addChild(pathPart, new JsonValue(JsonValue.ValueType.object));
            }
            node = node.get(pathPart);
        }
        return node;
    }

    /** Load the default configuration from the static fields in the ConfigKey classes */
    private static void loadDefault() {
        configRoot = new JsonValue(JsonValue.ValueType.object);
        Field[] fields = findConfigFields();
        for (Field field : fields) {
            try {
                ConfigKey<?> key = (ConfigKey<?>) field.get(null);
                JsonValue node = findOrCreate(key.path);
                node.set(key.value.serialize());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // Find all Classes with KeyMap annotation
    private static Field[] findConfigFields() {
        return Stream.of(CONFIG_CLASSES)
                .flatMap(clazz -> Stream.of(clazz.getDeclaredFields()))
                .filter(
                        field ->
                                field.getType() == ConfigKey.class
                                        && Modifier.isFinal(field.getModifiers())
                                        && Modifier.isStatic(field.getModifiers()))
                .toArray(Field[]::new);
    }

    /** Prepare the fields in the ConfigKey classes (use the path from the ConfigMap as prefix) */
    private static void prepareFields() {
        if (fieldsLoaded) return;
        Stream.of(findConfigFields())
                .filter(field -> field.getDeclaringClass().isAnnotationPresent(ConfigMap.class))
                .forEach(
                        field -> {
                            String[] mapPath =
                                    field.getDeclaringClass().getAnnotation(ConfigMap.class).path();
                            try {
                                ConfigKey<?> key = (ConfigKey<?>) field.get(null);
                                String[] path = new String[mapPath.length + key.path.length];
                                System.arraycopy(mapPath, 0, path, 0, mapPath.length);
                                System.arraycopy(
                                        key.path, 0, path, mapPath.length, key.path.length);
                                key.path = path;
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        });
        fieldsLoaded = true;
    }

    /**
     * Update the configuration with the given key
     *
     * @param key Key to update
     */
    protected static void update(ConfigKey<?> key) {
        JsonValue node = findOrCreate(key.path);
        node.set(key.value.serialize());
        saveConfiguration();
    }
}
