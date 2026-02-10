package core.level.loader;

import core.Game;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.utils.ClipboardUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for saving the current state of the dungeon in the game.
 *
 * @see DungeonLoader
 */
public class DungeonSaver {

  /**
   * Saves the current dungeon by printing it to the console. The output is also copied to the
   * system clipboard for easy pasting into a .level file.
   */
  public static void saveCurrentDungeon(boolean saveToFile, String pathToLevels) {
    ILevel currentLevel = Game.currentLevel().orElse(null);
    if (currentLevel == null) {
      System.out.println("No level to save.");
      return;
    }
    if (!(currentLevel instanceof DungeonLevel dunLevel)) {
      System.out.println("Current level is not a DungeonLevel. Cannot save.");
      return;
    }

    String output = LevelParser.serializeLevel(dunLevel);
    System.out.println(output);
    ClipboardUtil.copyToClipboard(output);

    if (saveToFile) {
      String currentLevelFile = DungeonLoader.currentLevel();
      String fullPath = pathToLevels + currentLevelFile + "_1.level";
      try {
        Files.write(Paths.get(fullPath), output.getBytes());
        System.out.println("Level sucessfully saved: " + fullPath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
