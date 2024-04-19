package core.configuration;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import core.utils.components.path.IPath;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Provides a mechanism for managing and persisting configuration data.
 *
 * <p>This class allows loading, saving, and updating configuration settings from a specified file
 * path. It supports multiple configuration classes containing annotated fields, where each field
 * represents a configuration key. The keys are organized in a hierarchical structure, and the class
 * provides methods to load, save, and update configuration settings.
 *
 * <p>The `Configuration` class utilizes the libGDX library's `JsonValue` for handling JSON data,
 * providing a convenient way to work with configuration files.
 *
 * @see ConfigKey
 * @see ConfigMap
 * @see KeyboardConfig
 */
public class Configuration {

  private static final HashMap<IPath, Configuration> LOADED_CONFIGURATION_FILES = new HashMap<>();
  private static final JsonValue.PrettyPrintSettings PRETTY_PRINT_SETTINGS =
      new JsonValue.PrettyPrintSettings();
  private final Class<?>[] configClasses;
  private final IPath configFilePath;
  private boolean fieldsLoaded = false;
  private JsonValue configRoot;

  private Configuration(Class<?>[] configMapClasses, IPath configFilePath) {
    this.configFilePath = configFilePath;
    configClasses = configMapClasses;
    PRETTY_PRINT_SETTINGS.outputType = JsonWriter.OutputType.json;
    PRETTY_PRINT_SETTINGS.wrapNumericArrays = true;
    PRETTY_PRINT_SETTINGS.singleLineColumns = 0;
    prepareFields();
  }

  /**
   * Load the configuration from the given path and return it. If the configuration has already been
   * loaded, the cached version will be returned.
   *
   * @param path Path to the configuration file
   * @param configMapClasses Classes where the ConfigKey fields are located (may be an annotated
   *     with ConfigMap annotation)
   * @return Configuration
   * @throws IOException If the file could not be read
   */
  public static Configuration loadAndGetConfiguration(IPath path, Class<?>... configMapClasses)
      throws IOException {
    if (LOADED_CONFIGURATION_FILES.containsKey(path)) {
      return LOADED_CONFIGURATION_FILES.get(path);
    }
    Configuration config = new Configuration(configMapClasses, path);
    config.load();
    LOADED_CONFIGURATION_FILES.put(path, config);
    return config;
  }

  /**
   * Loads the configuration from the given path.
   *
   * <p>SIDE EFFECT: Will create a new file containing a default configuration, if (a) there is no
   * configuration file, or (b) if the file exists, but does not contain valid JSON.
   *
   * @throws IOException If the file could not be read
   */
  private void load() throws IOException {
    File file = new File(configFilePath.pathString());

    if (!file.exists()) {
      createAndLoadDefaultConfiguration(file);
      return;
    }

    try (InputStreamReader isr =
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
      JsonReader jsonReader = new JsonReader();
      configRoot = jsonReader.parse(isr);
    } catch (IOException e) {
      throw new IOException(String.join(" ", "Failed to load game configuration.", e.getMessage()));
    }

    if (configRoot == null) {
      loadDefault();
      saveConfiguration();
    }

    // WTF? What happens here?
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

  // Create file & load default config if not exists
  private void createAndLoadDefaultConfiguration(File file) throws IOException {
    try {
      if (file.createNewFile()) {
        loadDefault();
        saveConfiguration();
      }
    } catch (IOException e) {
      throw new IOException(
          String.join(" ", "Failed to create a new default game configuration.", e.getMessage()));
    }
  }

  /** Save the current configuration to the file. */
  public void saveConfiguration() {
    try {
      File file = new File(configFilePath.pathString());
      FileOutputStream fos = new FileOutputStream(file, false);
      OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
      osw.write(configRoot.prettyPrint(PRETTY_PRINT_SETTINGS));
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

  /** Load the default configuration from the static fields in the ConfigKey classes. */
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

  /** Prepare the fields in the ConfigKey classes (use the path from the ConfigMap as prefix). */
  private void prepareFields() {
    if (fieldsLoaded) return;
    Stream.of(findConfigFields())
        .filter(field -> field.getDeclaringClass().isAnnotationPresent(ConfigMap.class))
        .forEach(
            field -> {
              String[] mapPath = field.getDeclaringClass().getAnnotation(ConfigMap.class).path();
              try {
                ConfigKey<?> key = (ConfigKey<?>) field.get(null);
                key.configuration = this;
                String[] path = new String[mapPath.length + key.path.length];
                System.arraycopy(mapPath, 0, path, 0, mapPath.length);
                System.arraycopy(key.path, 0, path, mapPath.length, key.path.length);
                key.path = path;
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              }
            });
    fieldsLoaded = true;
  }

  /**
   * Update the configuration with the given key.
   *
   * @param key Key to update
   */
  protected void update(ConfigKey<?> key) {
    JsonValue node = findOrCreate(key.path);
    node.set(key.value.serialize());
    saveConfiguration();
  }
}
