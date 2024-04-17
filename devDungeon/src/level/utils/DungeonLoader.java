package level.utils;

import core.Game;
import core.level.elements.ILevel;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import level.DevDungeonLevel;

public class DungeonLoader {

  private static final Logger LOGGER = Logger.getLogger(DungeonLoader.class.getSimpleName());
  private static final Random RANDOM = new Random();
  private static final String LEVEL_PATH_PREFIX = "/levels";
  private static final Map<String, List<String>> LEVELS = new HashMap<>();

  static {
    getAllLevelFilePaths();
  }

  private final String[] levelOrder;
  private int currentLevel = 0;

  /**
   * Constructs a new DungeonLoader with the specified level order.
   *
   * <p>This constructor takes an array of level names and converts them to lowercase. This is done
   * because all level names in the game are expected to be in lowercase. The level order array is
   * used to determine the order in which levels are loaded in the game.
   *
   * @param levelOrder An array of level names in the order they should be loaded.
   */
  public DungeonLoader(String[] levelOrder) {
    for (int i = 0; i < levelOrder.length; i++) {
      levelOrder[i] = levelOrder[i].toLowerCase(); // all level names should be lowercase
    }

    this.levelOrder = levelOrder;
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
    return Objects.requireNonNull(
            DungeonLoader.class.getResource(DungeonLoader.class.getSimpleName() + ".class"))
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
   * Returns the current level order.
   *
   * @return The current level order.
   */
  public String[] levelOrder() {
    return this.levelOrder;
  }

  /**
   * Returns the name of the current level (in lowercase).
   *
   * @return The name of the current level.
   */
  public String currentLevel() {
    return this.levelOrder()[this.currentLevel];
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
      Game.currentLevel(this.getRandomVariant(this.levelOrder[this.currentLevel]));
    } catch (MissingLevelException | ArrayIndexOutOfBoundsException e) {
      System.out.println("Game Over!");
      System.out.println("You have passed all " + this.currentLevel + " levels!");
      Game.exit();
    }
  }

  /**
   * Loads a specific level (with a random variant).
   *
   * @param levelName The name of the level.
   */
  public void loadLevel(String levelName) {
    this.setCurrentLevelByLevelName(levelName);
    Game.currentLevel(this.getRandomVariant(levelName));
  }

  /**
   * Loads a specific level variant.
   *
   * @param levelName The name of the level.
   * @param variant The index of the level variant.
   */
  public void loadLevel(String levelName, int variant) {
    this.setCurrentLevelByLevelName(levelName);
    List<String> levelVariants = LEVELS.get(levelName);
    if (levelVariants == null || levelVariants.isEmpty() || variant >= levelVariants.size()) {
      throw new MissingLevelException(levelName);
    }
    IPath levelPath = new SimpleIPath(levelVariants.get(variant));
    Game.currentLevel(DevDungeonLevel.loadFromPath(levelPath));
  }

  private void setCurrentLevelByLevelName(String levelName) {
    this.currentLevel = -1;
    for (int i = 0; i < this.levelOrder.length; i++) {
      if (this.levelOrder[i].equals(levelName)) {
        this.currentLevel = i;
        break;
      }
    }

    if (this.currentLevel == -1) {
      throw new MissingLevelException(levelName);
    }
  }

  /**
   * Returns the index of the current level.
   *
   * @return The index of the current level.
   */
  public int currentLevelIndex() {
    return this.currentLevel;
  }
}
