package core.level.loader;

import core.Game;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.utils.ClipboardUtil;

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
  public static void saveCurrentDungeon() {
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
  }
}
