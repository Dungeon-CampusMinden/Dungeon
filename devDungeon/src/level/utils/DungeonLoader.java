package level.utils;

import core.Game;
import core.level.elements.ILevel;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  private static void getAllLevelFilePaths() { // TODO: only works with local file-system
    try {
      URI uri = Objects.requireNonNull(DungeonLoader.class.getResource(LEVEL_PATH_PREFIX)).toURI();
      Path path = Paths.get(uri);
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
                      LEVELS.computeIfAbsent(levelName, k -> new ArrayList<>()).add(levelFilePath);
                    } else {
                      LOGGER.warning("Invalid level file name: " + fileName);
                    }
                  }
                });
      }
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Error loading level files", e);
    }
  }

  public String[] levelOrder() {
    return this.levelOrder;
  }

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

  public void loadNextLevel() {
    this.currentLevel++;
    try {
      Game.currentLevel(this.getRandomVariant(this.levelOrder[this.currentLevel]));
    } catch (MissingLevelException e) {
      System.out.println("Game Over!");
      System.out.println("You have passed all " + this.currentLevel + " levels!");
      Game.exit();
    }
  }

  public void loadLevel(String levelName) {
    this.setCurrentLevelByLevelName(levelName);
    Game.currentLevel(this.getRandomVariant(levelName));
  }

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
}
