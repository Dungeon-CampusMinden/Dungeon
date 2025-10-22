package core.configuration;

import core.utils.JsonHandler;
import core.utils.components.path.IPath;
import core.utils.logging.DungeonLogger;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Provides a mechanism for managing and persisting configuration data.
 *
 * <p>This class allows loading, saving, and updating configuration settings from a specified file
 * path. It supports multiple configuration classes containing annotated fields, where each field
 * represents a configuration key. The keys are organized using a path array which translates to
 * nested map structures. The class provides methods to load, save, and update configuration
 * settings using {@link JsonHandler}.
 *
 * <p>Configuration files are cached based on their {@link IPath}.
 *
 * @see ConfigKey
 * @see ConfigMap
 * @see KeyboardConfig
 */
public class Configuration {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Configuration.class);
  private static final HashMap<IPath, Configuration> LOADED_CONFIGURATION_FILES = new HashMap<>();

  private final Class<?>[] configClasses;
  private final IPath configFilePath;
  private boolean fieldsPrepared = false;
  private Map<String, Object> configRoot;

  private Configuration(Class<?>[] configMapClasses, IPath configFilePath) {
    this.configFilePath = configFilePath;
    this.configClasses = configMapClasses;
    prepareConfigKeyPaths();
  }

  /**
   * Retrieves a value from the nested configRoot map.
   *
   * @param path Path segments to the value.
   * @return The value, or null if not found or path is invalid.
   */
  @SuppressWarnings("unchecked")
  private Object getValueFromPath(String[] path) {
    if (configRoot == null || path == null || path.length == 0) {
      return null;
    }
    Map<String, Object> currentMap = configRoot;
    for (int i = 0; i < path.length - 1; i++) {
      Object node = currentMap.get(path[i]);
      if (node instanceof Map) {
        currentMap = (Map<String, Object>) node;
      } else {
        return null; // Path does not lead to a map or is broken
      }
    }
    return currentMap.get(path[path.length - 1]);
  }

  /**
   * Sets a value in the nested configRoot map, creating intermediate maps if necessary.
   *
   * @param path Path segments to the value.
   * @param value The value to set.
   */
  @SuppressWarnings("unchecked")
  private void setValueInPath(String[] path, Object value) {
    if (configRoot == null) {
      configRoot = new HashMap<>();
    }
    if (path == null || path.length == 0) {
      LOGGER.warn("Attempted to set value with no path.");
      return;
    }
    Map<String, Object> currentMap = configRoot;
    for (int i = 0; i < path.length - 1; i++) {
      currentMap =
          (Map<String, Object>)
              currentMap.computeIfAbsent(path[i], k -> new HashMap<String, Object>());
    }
    currentMap.put(path[path.length - 1], value);
  }

  /**
   * Loads the configuration from the given path and returns it. If the configuration has already
   * been loaded (cached by path), the cached version will be returned.
   *
   * @param path Path to the configuration file.
   * @param configMapClasses Classes where the {@link ConfigKey} fields are located (may be
   *     annotated with {@link ConfigMap}).
   * @return The loaded {@link Configuration} instance.
   * @throws IOException If the file could not be read during an initial load.
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
   * Loads the configuration from the file path specified at construction.
   *
   * <p>SIDE EFFECT: Will create a new file containing a default configuration if:
   *
   * <ul>
   *   <li>There is no configuration file at the specified path.
   *   <li>The file exists but does not contain valid JSON or is empty.
   * </ul>
   *
   * <p>Any missing keys in an existing configuration will be added with their default values.
   *
   * @throws IOException If the file could not be read.
   */
  private void load() throws IOException {
    File file = new File(configFilePath.pathString());

    if (!file.exists()) {
      LOGGER.info(
          "Configuration file not found: "
              + configFilePath.pathString()
              + ". Creating with default values.");
      createAndLoadDefaultConfiguration(file);
      return;
    }

    String fileContent = readFileContent(file);

    if (fileContent.trim().isEmpty()) {
      LOGGER.info(
          "Configuration file is empty: "
              + configFilePath.pathString()
              + ". Loading default configuration and saving.");
      loadDefaultAndSave();
      return;
    }

    try {
      configRoot = JsonHandler.readJson(fileContent);
    } catch (IllegalArgumentException e) {
      LOGGER.warn(
          "Failed to parse existing configuration from "
              + configFilePath.pathString()
              + ". Loading default configuration and saving.",
          e);
      loadDefaultAndSave();
      return;
    }

    boolean configurationModified = processLoadedKeysAndUpdateConfigRoot();
    if (configurationModified) {
      LOGGER.info("Configuration was modified during load (e.g., defaults added). Saving changes.");
      saveConfiguration();
    }
  }

  private String readFileContent(File file) throws IOException {
    try (InputStream fis = new FileInputStream(file)) {
      return new String(fis.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOGGER.fatal("Failed to read configuration file: {}", file.getPath(), e);
      throw new IOException("Failed to read game configuration file: " + file.getPath(), e);
    }
  }

  private void createAndLoadDefaultConfiguration(File file) throws IOException {
    try {
      if (file.createNewFile()) {
        LOGGER.info("Created new configuration file: " + file.getPath());
        loadDefaultAndSave();
      } else {
        // This case should ideally not be reached if file.exists() was false before.
        // If it is, it might indicate a race condition or an issue with file system state.
        LOGGER.warn(
            "File reported as non-existent, but createNewFile failed to create it (already exists?): "
                + file.getPath()
                + ". Attempting to load defaults and save.");
        loadDefaultAndSave();
      }
    } catch (IOException e) {
      LOGGER.fatal("Failed to create a new default game configuration at: {}", file.getPath(), e);
      throw new IOException(
          "Failed to create a new default game configuration: " + file.getPath(), e);
    }
  }

  private void loadDefaultAndSave() {
    loadDefault();
    saveConfiguration();
  }

  /**
   * Processes all declared {@link ConfigKey} fields. For each key, it checks if it exists in the
   * current {@code configRoot}. If not, or if the value is null, it sets the key to its default
   * value. Otherwise, it attempts to deserialize the value from {@code configRoot} into the {@link
   * ConfigKey}.
   *
   * @return {@code true} if any key was set to its default value (indicating the configuration was
   *     modified), {@code false} otherwise.
   */
  private boolean processLoadedKeysAndUpdateConfigRoot() {
    AtomicBoolean dirty = new AtomicBoolean(false);
    Field[] configFields = findConfigKeyFields();

    for (Field field : configFields) {
      ConfigKey<?> configKeyInstance = null;
      try {
        configKeyInstance = (ConfigKey<?>) field.get(null);
        Object valueFromConfig = getValueFromPath(configKeyInstance.path);

        if (valueFromConfig == null) {
          LOGGER.info(
              String.format(
                  "Configuration key '%s' (field: %s) not found or value is null. Setting to default.",
                  String.join(".", configKeyInstance.path), field.getName()));
          setValueInPath(configKeyInstance.path, configKeyInstance.value.serialize());
          dirty.set(true);
        } else {
          try {
            configKeyInstance.value.deserialize(String.valueOf(valueFromConfig));
          } catch (Exception deserializeEx) {
            LOGGER.warn(
                String.format(
                    "Error deserializing value for config key '%s' (field: %s). Value: '%s'. Setting to default.",
                    String.join(".", configKeyInstance.path), field.getName(), valueFromConfig),
                deserializeEx);
            setValueInPath(configKeyInstance.path, configKeyInstance.value.serialize());
            dirty.set(true);
          }
        }
      } catch (IllegalAccessException iae) {
        LOGGER.fatal(
            "CRITICAL: Cannot access static ConfigKey field: "
                + field.getName()
                + ". Configuration for this key cannot be processed.",
            iae);
        // This is a programming error or a severe access issue.
      } catch (Exception e) { // Catch-all for other unexpected issues with this field
        String pathInfo =
            (configKeyInstance != null)
                ? String.join(".", configKeyInstance.path)
                : "unknown (ConfigKey access failed)";
        LOGGER.warn(
            String.format(
                "Unexpected error processing config field '%s' (path: %s). Attempting to set to default if possible.",
                field.getName(), pathInfo),
            e);
        if (configKeyInstance != null) {
          try {
            setValueInPath(configKeyInstance.path, configKeyInstance.value.serialize());
            dirty.set(true);
          } catch (Exception exSetDefault) {
            LOGGER.error(
                String.format(
                    "Failed to set default value for config field '%s' (path: %s) after a previous error.",
                    field.getName(), pathInfo),
                exSetDefault);
          }
        } else {
          LOGGER.error(
              "Cannot set default for field '{}' as ConfigKey instance could not be retrieved.",
              field.getName());
        }
      }
    }
    return dirty.get();
  }

  /** Saves the current configuration ({@code configRoot}) to the file. */
  public void saveConfiguration() {
    if (this.configRoot == null) {
      LOGGER.warn(
          "Attempted to save configuration, but configRoot is null. Initializing to default before saving.");
      loadDefault(); // Initialize configRoot with defaults
      if (this.configRoot == null) { // Should not happen if loadDefault works
        LOGGER.error(
            "Failed to initialize configRoot even after loadDefault. Aborting save operation for "
                + configFilePath.pathString());
        return;
      }
    }

    try {
      File file = new File(configFilePath.pathString());
      // Ensure parent directory exists
      File parentDir = file.getParentFile();
      if (parentDir != null && !parentDir.exists()) {
        if (!parentDir.mkdirs()) {
          LOGGER.warn(
              "Failed to create parent directories for configuration file: " + file.getPath());
          // Continue attempting to write, FileOutputStream might handle it or fail
        }
      }

      String jsonString = JsonHandler.writeJson(this.configRoot, true);
      try (OutputStreamWriter osw =
          new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
        osw.write(jsonString);
      }
      LOGGER.info("Configuration saved to: " + configFilePath.pathString());
    } catch (IOException e) {
      LOGGER.error("Error saving configuration to: {}", configFilePath.pathString(), e);
    } catch (Exception e) {
      LOGGER.error(
          "Unexpected error during configuration save to: {}", configFilePath.pathString(), e);
    }
  }

  /**
   * Loads the default configuration by initializing {@code configRoot} and populating it with
   * default values from all declared {@link ConfigKey} fields.
   */
  private void loadDefault() {
    configRoot = new HashMap<>();
    Field[] fields = findConfigKeyFields();
    for (Field field : fields) {
      try {
        ConfigKey<?> key = (ConfigKey<?>) field.get(null);
        setValueInPath(key.path, key.value.serialize());
      } catch (IllegalAccessException e) {
        LOGGER.fatal(
            "Error accessing ConfigKey field '"
                + field.getName()
                + "' during loadDefault. This key's default value could not be set.",
            e);
      } catch (Exception e) {
        LOGGER.warn(
            "Unexpected error processing ConfigKey field '"
                + field.getName()
                + "' during loadDefault.",
            e);
      }
    }
    LOGGER.info("Default configuration values loaded into memory.");
  }

  /**
   * Finds all static final fields of type {@link ConfigKey} in the configured {@code
   * configClasses}.
   *
   * @return An array of {@link Field} objects representing the config keys.
   */
  private Field[] findConfigKeyFields() {
    return Stream.of(configClasses)
        .flatMap(clazz -> Stream.of(clazz.getDeclaredFields()))
        .filter(
            field ->
                field.getType() == ConfigKey.class
                    && Modifier.isFinal(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers()))
        .toArray(Field[]::new);
  }

  /**
   * Prepares the {@link ConfigKey} fields by adjusting their paths. If a {@link ConfigKey}'s
   * declaring class is annotated with {@link ConfigMap}, the path from {@link ConfigMap} is used as
   * a prefix for the {@link ConfigKey}'s path. This method also sets the {@code configuration}
   * field of each {@link ConfigKey} to this {@link Configuration} instance.
   *
   * <p>This preparation runs once per {@link Configuration} instance.
   *
   * <p><b>Note:</b> This method modifies the {@code path} field of static {@link ConfigKey}
   * instances. If the same {@link ConfigKey} classes are used by multiple {@link Configuration}
   * instances with different {@link ConfigMap} paths (and not managed by the static cache {@code
   * LOADED_CONFIGURATION_FILES}), this could lead to unexpected path nesting.
   */
  private void prepareConfigKeyPaths() {
    if (fieldsPrepared) {
      return;
    }
    Field[] configFields = findConfigKeyFields();
    Stream.of(configFields)
        .forEach(
            field -> {
              try {
                ConfigKey<?> key = (ConfigKey<?>) field.get(null);
                key.configuration = this; // Link key to this configuration instance

                Class<?> declaringClass = field.getDeclaringClass();
                if (declaringClass.isAnnotationPresent(ConfigMap.class)) {
                  String[] mapPath = declaringClass.getAnnotation(ConfigMap.class).path();
                  // Create new path by prefixing mapPath to key's current path
                  // Assumes key.path initially holds its relative path.
                  String[] currentKeyPath = key.path;
                  String[] newPath = new String[mapPath.length + currentKeyPath.length];
                  System.arraycopy(mapPath, 0, newPath, 0, mapPath.length);
                  System.arraycopy(
                      currentKeyPath, 0, newPath, mapPath.length, currentKeyPath.length);
                  key.path = newPath;
                }
              } catch (IllegalAccessException e) {
                LOGGER.fatal(
                    "Cannot access static ConfigKey field during path preparation: "
                        + field.getName(),
                    e);
                throw new RuntimeException(
                    "Failed to prepare configuration field paths due to access error on: "
                        + field.getName(),
                    e);
              } catch (Exception e) {
                LOGGER.error(
                    "Unexpected error during ConfigKey path preparation for field: {}",
                    field.getName(),
                    e);
                // Depending on severity, could rethrow.
              }
            });
    fieldsPrepared = true;
  }

  /**
   * Updates the configuration with the given key's current value and saves the configuration.
   *
   * @param key The {@link ConfigKey} to update in the configuration.
   */
  protected void update(ConfigKey<?> key) {
    if (configRoot == null) {
      LOGGER.warn(
          "ConfigRoot is null during update for key path: "
              + String.join(".", key.path)
              + ". Attempting to load defaults first.");
      loadDefault(); // This will initialize configRoot
      if (configRoot == null) {
        LOGGER.error(
            "Failed to initialize configRoot in update even after loadDefault. Aborting update for key path: "
                + String.join(".", key.path));
        return;
      }
    }
    setValueInPath(key.path, key.value.serialize());
    saveConfiguration();
  }
}
