package contrib.level;

import contrib.utils.level.MissingLevelException;
import core.Game;
import core.level.elements.ILevel;
import core.utils.IVoidFunction;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The DevDungeonLoader class is used to load {@link DevDungeonLevel} in the game. It is used to
 * load levels in a specific order or to load a specific level. The DungeonLoader class is used to
 * load levels from the file system or from a jar file.
 *
 * @see DevDungeonLevel
 */
public class DevDungeonLoader {

  private static final Logger LOGGER = Logger.getLogger(DevDungeonLoader.class.getSimpleName());
  private static final Random RANDOM = new Random();
  private static final String LEVEL_PATH_PREFIX = "/levels";
  private static final Map<String, List<String>> LEVELS = new HashMap<>();

  static {
    getAllLevelFilePaths();
  }

  private static final List<Tuple<String, Class<? extends DevDungeonLevel>>> levelOrder =
      new ArrayList<>();
  private static int currentLevel = 0;
  private static int currentVariant = 0;
  private static IVoidFunction afterAllLevels =
      () -> {
        System.out.println("Game Over!");
        System.out.println("You have passed all " + currentLevel + " levels!");
        Game.exit();
      };

  // Private constructor to prevent instantiation, as this class is a static utility class.
  private DevDungeonLoader() {}

  private static void getAllLevelFilePaths() {
    if (isRunningFromJar()) {
      try {
        getAllLevelFilePathsFromJar();
      } catch (IOException | URISyntaxException e) {
        LOGGER.warning("Failed to load level files from jar: " + e.getMessage());
      }
    } else {
      try {
        getAllLevelFilePathsFromFileSystem();
      } catch (IOException | URISyntaxException e) {
        LOGGER.warning("Failed to load level files from file system: " + e.getMessage());
      }
    }
  }

  private static boolean isRunningFromJar() {
    return Objects.requireNonNull(DevDungeonLoader.class.getResource(LEVEL_PATH_PREFIX))
        .toString()
        .startsWith("jar:");
  }

  private static void getAllLevelFilePathsFromFileSystem() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(DevDungeonLoader.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    Path path = Paths.get(uri);
    parseLevelFiles(path, false);
  }

  private static void getAllLevelFilePathsFromJar() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(DevDungeonLoader.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
    Path path = fileSystem.getPath(LEVEL_PATH_PREFIX);
    parseLevelFiles(path, true);
  }

  private static void parseLevelFiles(Path path, boolean isJar) throws IOException {
    try (Stream<Path> paths = Files.walk(path)) {
      paths
          .filter(Files::isRegularFile)
          .forEach(
              file -> {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".level")) {
                  String[] parts = fileName.split("_");
                  if (parts.length == 2) {
                    String levelName = parts[0];
                    String levelFilePath = file.toString();
                    LEVELS
                        .computeIfAbsent(levelName, k -> new ArrayList<>())
                        .add(isJar ? "jar:" + levelFilePath : levelFilePath);
                  } else {
                    LOGGER.warning("Invalid level file name: " + fileName);
                  }
                }
              });
    }
  }

  /**
   * Adds a level to the level order.
   *
   * @param level A {@link Tuple} containing the level name and the level handler class. The level
   *     name is converted to lowercase.
   * @see #levelOrder()
   * @see #loadNextLevel()
   */
  @SafeVarargs
  public static void addLevel(Tuple<String, Class<? extends DevDungeonLevel>>... level) {
    for (Tuple<String, Class<? extends DevDungeonLevel>> t : level) {
      levelOrder.add(new Tuple<>(t.a().toLowerCase(), t.b()));
    }
  }

  /**
   * Returns the current level order.
   *
   * @return The current level order.
   */
  public static List<String> levelOrder() {
    return levelOrder.stream().map(Tuple::a).toList();
  }

  /**
   * Returns the name of the current level (in lowercase).
   *
   * @return The name of the current level.
   * @throws IndexOutOfBoundsException If the current level index is out of bounds.
   */
  public static String currentLevel() {
    return levelOrder.get(currentLevel).a().toLowerCase();
  }

  /**
   * Returns the level handler for the given level name.
   *
   * @param levelName The name of the level.
   * @return The level handler for the given level name. (null if not found)
   * @see DevDungeonLevel
   */
  public static Class<? extends DevDungeonLevel> levelHandler(String levelName) {
    for (Tuple<String, Class<? extends DevDungeonLevel>> level : levelOrder) {
      if (level.a().equalsIgnoreCase(levelName)) {
        return level.b();
      }
    }
    return null;
  }

  private static ILevel getRandomVariant(String levelName) {
    List<String> levelVariants = LEVELS.get(levelName);

    if (levelVariants == null || levelVariants.isEmpty()) {
      throw new MissingLevelException(levelName);
    }

    // Random Level Variant Path
    currentVariant = RANDOM.nextInt(levelVariants.size());
    IPath levelPath = new SimpleIPath(levelVariants.get(currentVariant));

    return DevDungeonLevel.loadFromPath(levelPath);
  }

  /**
   * Loads the next level in the level order.
   *
   * <p>If the current level is the last level in the level order, it will execute the callback
   * function set by {@link #afterAllLevels(IVoidFunction)}. Default is to close the game with a
   * "Game Over!" message.
   *
   * <p>It chooses a random variant of the next level.
   */
  public static void loadNextLevel() {
    DevDungeonLoader.currentLevel++;
    try {
      Game.currentLevel(getRandomVariant(currentLevel()));
    } catch (MissingLevelException | IndexOutOfBoundsException e) {
      afterAllLevels.execute();
    }
  }

  /**
   * Sets the callback function that will be executed after all levels are completed.
   *
   * <p>Default is to close the game with a "Game Over!" message.
   *
   * @param afterAllLevels The callback function to execute after all levels are completed.
   * @see IVoidFunction
   * @see #loadNextLevel()
   */
  public static void afterAllLevels(IVoidFunction afterAllLevels) {
    DevDungeonLoader.afterAllLevels = afterAllLevels;
  }

  /**
   * Loads a specific level (with a random variant).
   *
   * @param levelName The name of the level.
   */
  public static void loadLevel(String levelName) {
    setCurrentLevelByLevelName(levelName);
    Game.currentLevel(getRandomVariant(levelName));
  }

  /**
   * Loads a specific level (with a random variant).
   *
   * @param levelIndex The index of the level.
   * @throws IndexOutOfBoundsException If the level index is out of bounds.
   */
  public static void loadLevel(int levelIndex) {
    if (levelIndex < 0 || levelIndex >= levelOrder.size()) {
      throw new IndexOutOfBoundsException("Level index is out of bounds: " + levelIndex);
    }
    loadLevel(levelOrder.get(levelIndex).a());
  }

  /**
   * Loads a specific level variant.
   *
   * @param levelName The name of the level.
   * @param variant The index of the level variant.
   */
  public static void loadLevel(String levelName, int variant) {
    setCurrentLevelByLevelName(levelName);
    List<String> levelVariants = LEVELS.get(levelName);
    if (levelVariants == null || levelVariants.isEmpty() || variant >= levelVariants.size()) {
      throw new MissingLevelException(levelName);
    }

    currentVariant = variant;
    IPath levelPath = new SimpleIPath(levelVariants.get(variant));
    Game.currentLevel(DevDungeonLevel.loadFromPath(levelPath));
  }

  private static void setCurrentLevelByLevelName(String levelName) {
    DevDungeonLoader.currentLevel = -1;
    for (int i = 0; i < levelOrder.size(); i++) {
      if (levelOrder.get(i).a().equals(levelName)) {
        DevDungeonLoader.currentLevel = i;
        break;
      }
    }

    if (currentLevel == -1) {
      throw new MissingLevelException(levelName);
    }
  }

  /**
   * Reloads the current level with the current variant.
   *
   * <p>This method is useful for resetting the level state without changing the level itself.
   */
  public static void reloadCurrentLevel() {
    if (currentLevel < 0 || currentLevel >= levelOrder.size()) {
      throw new IndexOutOfBoundsException("Current level index is out of bounds: " + currentLevel);
    }
    loadLevel(levelOrder.get(currentLevel).a(), currentVariant);
  }

  /**
   * Returns the index of the current level.
   *
   * @return The index of the current level.
   */
  public static int currentLevelIndex() {
    return currentLevel;
  }

  /**
   * Returns the current variant index of the level.
   *
   * @return The current variant index of the level.
   */
  public static int currentVariantIndex() {
    return currentVariant;
  }
}
