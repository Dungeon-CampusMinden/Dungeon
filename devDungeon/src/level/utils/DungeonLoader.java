package level.utils;

import core.Game;
import core.level.elements.ILevel;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import level.DevDungeonLevel;

/**
 * The DungeonLoader class is used to load levels in the game. It is used to load levels in a
 * specific order or to load a specific level. The DungeonLoader class is used to load levels from
 * the file system or from a jar file.
 *
 * @see DevDungeonLevel
 */
public class DungeonLoader {

  // Singleton instance
  private static DungeonLoader instance;

  private static final Logger LOGGER = Logger.getLogger(DungeonLoader.class.getSimpleName());
  private static final Random RANDOM = new Random();
  private static final String LEVEL_PATH_PREFIX = "/levels";
  private static final Map<String, List<String>> LEVELS = new HashMap<>();

  static {
    getAllLevelFilePaths();
  }

  private final List<Tuple<String, Class<? extends DevDungeonLevel>>> levelOrder =
      new ArrayList<>();
  private int currentLevel = 0;

  private DungeonLoader() {}

  /**
   * Returns the instance of the DungeonLoader.
   *
   * @return The instance of the DungeonLoader.
   */
  public static DungeonLoader instance() {
    if (instance == null) {
      instance = new DungeonLoader();
    }
    return instance;
  }

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
    return Objects.requireNonNull(DungeonLoader.class.getResource(LEVEL_PATH_PREFIX))
        .toString()
        .startsWith("jar:");
  }

  private static void getAllLevelFilePathsFromFileSystem() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(DungeonLoader.class.getResource(LEVEL_PATH_PREFIX)).toURI();
    Path path = Paths.get(uri);
    parseLevelFiles(path, false);
  }

  private static void getAllLevelFilePathsFromJar() throws IOException, URISyntaxException {
    URI uri = Objects.requireNonNull(DungeonLoader.class.getResource(LEVEL_PATH_PREFIX)).toURI();
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
   * @param level The name of the level. (it will be converted to lowercase)
   * @see #levelOrder()
   * @see #loadNextLevel()
   */
  public void addLevel(Tuple<String, Class<? extends DevDungeonLevel>>... level) {
    for (Tuple<String, Class<? extends DevDungeonLevel>> levelEntry : level) {
      levelOrder.add(levelEntry);
    }
  }

  /**
   * Returns the current level order.
   *
   * @return The current level order.
   */
  public Set<String> levelOrder() {
    return levelOrder.stream().map(Tuple::a).collect(Collectors.toSet());
  }

  /**
   * Returns the name of the current level (in lowercase).
   *
   * @return The name of the current level.
   * @throws IndexOutOfBoundsException If the current level index is out of bounds.
   */
  public String currentLevel() {
    return levelOrder.get(currentLevel).a().toLowerCase();
  }

  /**
   * Returns the level handler for the given level name.
   *
   * @param levelName The name of the level.
   * @return The level handler for the given level name. (null if not found)
   * @see DevDungeonLevel
   */
  public Class<? extends DevDungeonLevel> levelHandler(String levelName) {
    for (Tuple<String, Class<? extends DevDungeonLevel>> level : levelOrder) {
      if (level.a().equalsIgnoreCase(levelName)) {
        return level.b();
      }
    }
    return null;
  }

  private ILevel getRandomVariant(String levelName) {
    List<String> levelVariants = LEVELS.get(levelName);

    if (levelVariants == null || levelVariants.isEmpty()) {
      throw new MissingLevelException(levelName);
    }

    // Random Level Variant Path
    IPath levelPath = new SimpleIPath(levelVariants.get(RANDOM.nextInt(levelVariants.size())));

    return DevDungeonLevel.loadFromPath(levelPath);
  }

  /**
   * Loads the next level in the level order.
   *
   * <p>If the current level is the last level in the level order, the game will exit.
   *
   * <p>It chooses a random variant of the next level.
   */
  public void loadNextLevel() {
    this.currentLevel++;
    try {
      Game.currentLevel(getRandomVariant(currentLevel()));
    } catch (MissingLevelException | IndexOutOfBoundsException e) {
      System.out.println("Game Over!");
      System.out.println("You have passed all " + currentLevel + " levels!");
      Game.exit();
    }
  }

  /**
   * Loads a specific level (with a random variant).
   *
   * @param levelName The name of the level.
   */
  public void loadLevel(String levelName) {
    setCurrentLevelByLevelName(levelName);
    Game.currentLevel(getRandomVariant(levelName));
  }

  /**
   * Loads a specific level (with a random variant).
   *
   * @param levelIndex The index of the level.
   * @throws IndexOutOfBoundsException If the level index is out of bounds.
   */
  public void loadLevel(int levelIndex) {
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
  public void loadLevel(String levelName, int variant) {
    setCurrentLevelByLevelName(levelName);
    List<String> levelVariants = LEVELS.get(levelName);
    if (levelVariants == null || levelVariants.isEmpty() || variant >= levelVariants.size()) {
      throw new MissingLevelException(levelName);
    }
    IPath levelPath = new SimpleIPath(levelVariants.get(variant));
    Game.currentLevel(DevDungeonLevel.loadFromPath(levelPath));
  }

  private void setCurrentLevelByLevelName(String levelName) {
    this.currentLevel = -1;
    for (int i = 0; i < levelOrder.size(); i++) {
      if (levelOrder.get(i).a().equals(levelName)) {
        this.currentLevel = i;
        break;
      }
    }

    if (currentLevel == -1) {
      throw new MissingLevelException(levelName);
    }
  }

  /**
   * Returns the index of the current level.
   *
   * @return The index of the current level.
   */
  public int currentLevelIndex() {
    return currentLevel;
  }
}
