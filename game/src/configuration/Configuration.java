package configuration;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Configuration {

    private static final HashMap<String, Configuration> loadedConfigurationFiles = new HashMap<>();
    private static final JsonValue.PrettyPrintSettings prettyPrintSettings =
            new JsonValue.PrettyPrintSettings();
    private final Class<?>[] configClasses;
    private boolean fieldsLoaded = false;
    private JsonValue configRoot;
    private final String configFilePath;

    private Configuration(Class<?>[] configMapClasses, String configFilePath) {
        this.configFilePath = configFilePath;
        configClasses = configMapClasses;
        prettyPrintSettings.outputType = JsonWriter.OutputType.json;
        prettyPrintSettings.wrapNumericArrays = true;
        prettyPrintSettings.singleLineColumns = 0;
        prepareFields();
    }

    /**
     * Loads the configuration from the given path
     *
     * @throws IOException If the file could not be read
     */
    private void load() throws IOException {
        File file = new File(configFilePath);
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

        AtomicBoolean dirty = new AtomicBoolean(false);
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
                            if (node.isNull() || node.asString() == null) {
                                node.set(configKey.value.serialize());
                                dirty.set(true);
                                return;
                            }
                            configKey.value.deserialize(node.asString());
                        });
        if (dirty.get()) {
            saveConfiguration();
        }
    }

    /** Save the current configuration to the file */
    public void saveConfiguration() {
        try {
            File file = new File(configFilePath);
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
    private JsonValue findOrCreate(String[] path) {
        JsonValue node = configRoot;
        for (int i = 0; i < path.length; i++) {
            String pathPart = path[i];
            if (i == path.length - 1 && !node.has(pathPart)) {
                node.addChild(pathPart, new JsonValue(JsonValue.ValueType.stringValue));
            } else if (i < path.length - 1 && !node.has(pathPart)) {
                node.addChild(pathPart, new JsonValue(JsonValue.ValueType.object));
            }
            node = node.get(pathPart);
        }
        return node;
    }

    /** Load the default configuration from the static fields in the ConfigKey classes */
    private void loadDefault() {
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
    private Field[] findConfigFields() {
        return Stream.of(configClasses)
                .flatMap(clazz -> Stream.of(clazz.getDeclaredFields()))
                .filter(
                        field ->
                                field.getType() == ConfigKey.class
                                        && Modifier.isFinal(field.getModifiers())
                                        && Modifier.isStatic(field.getModifiers()))
                .toArray(Field[]::new);
    }

    /** Prepare the fields in the ConfigKey classes (use the path from the ConfigMap as prefix) */
    private void prepareFields() {
        if (fieldsLoaded) return;
        Stream.of(findConfigFields())
                .filter(field -> field.getDeclaringClass().isAnnotationPresent(ConfigMap.class))
                .forEach(
                        field -> {
                            String[] mapPath =
                                    field.getDeclaringClass().getAnnotation(ConfigMap.class).path();
                            try {
                                ConfigKey<?> key = (ConfigKey<?>) field.get(null);
                                key.configuration = Optional.of(this);
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
    protected void update(ConfigKey<?> key) {
        JsonValue node = findOrCreate(key.path);
        node.set(key.value.serialize());
        saveConfiguration();
    }

    /**
     * Load the configuration from the given path and return it. If the configuration has already
     * been loaded, the cached version will be returned.
     *
     * @param path Path to the configuration file
     * @param configMapClasses Classes where the ConfigKey fields are located (may be annotated with
     *     ConfigMap annotation)
     * @return Configuration
     * @throws IOException If the file could not be read
     */
    public static Configuration loadAndGetConfiguration(String path, Class<?>... configMapClasses)
            throws IOException {
        if (loadedConfigurationFiles.containsKey(path)) {
            return loadedConfigurationFiles.get(path);
        }
        Configuration config = new Configuration(configMapClasses, path);
        config.load();
        loadedConfigurationFiles.put(path, config);
        return config;
    }
}
