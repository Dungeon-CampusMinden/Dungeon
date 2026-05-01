package core.level.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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
   *
   * @param pathToLevels the path to the folder where the level file is stored
   */
  public static void saveCurrentDungeon(String pathToLevels) {
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
    if (pathToLevels != null && !pathToLevels.isEmpty()) {
      String currentLevelFile = DungeonLoader.currentLevel();

      // Sauberes Zusammenfügen des Pfads
      FileHandle folder = Gdx.files.local(pathToLevels);
      FileHandle file = folder.child(currentLevelFile + "_1.level");

      try {
        file.writeString(output, false); // false = überschreibt die Datei
        System.out.println("Level successfully saved: " + file.path());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
