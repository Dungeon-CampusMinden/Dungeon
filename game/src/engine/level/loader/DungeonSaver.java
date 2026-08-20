package engine.level.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.utils.ClipboardUtil;

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
   * @param filePath the path of the file where the level is stored
   */
  public static void saveCurrentDungeon(String filePath) {
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
    if (filePath != null && !filePath.isEmpty()) {
      FileHandle file = Gdx.files.local(normalizeLevelFilePath(filePath));

      try {
        file.writeString(output, false); // false = überschreibt die Datei
        System.out.println("Level successfully saved: " + file.path());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Converts a user-provided path into the level file path used by the saver.
   *
   * <p>The existing filename extension is replaced with {@code .level}. If the filename does not
   * already contain an underscore suffix, {@code _1} is added before the extension.
   *
   * @param filePath the user-provided file path
   * @return the normalized level file path
   */
  public static String normalizeLevelFilePath(String filePath) {
    int lastSeparator = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
    int extensionStart = filePath.lastIndexOf('.');
    String pathWithoutExtension =
        extensionStart > lastSeparator ? filePath.substring(0, extensionStart) : filePath;
    int lastUnderscore = pathWithoutExtension.lastIndexOf('_');
    if (lastUnderscore <= lastSeparator) {
      pathWithoutExtension += "_1";
    }
    return pathWithoutExtension + ".level";
  }
}
