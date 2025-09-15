package core.level.loader;

import core.Game;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.utils.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for saving the current state of the dungeon in the game.
 *
 * @see DungeonLoader
 */
public class DungeonSaver {

  /**
   * The saveCurrentDungeon method is responsible for saving the current state of the dungeon. It
   * does this by first getting the design label of the current level. Then it gets the position of
   * the start tile of the current level. After that, it compresses the layout of the current level
   * by removing all lines that only contain Empty Tiles. Finally, it concatenates all this
   * information into a single string and prints it.
   *
   * @return The string representation of the current dungeon state.
   */
  public static String saveCurrentDungeon() {
    String designLabel =
        Game.currentLevel()
            .flatMap(ILevel::designLabel)
            .map(DesignLabel::name)
            .orElse(DesignLabel.DEFAULT.name());

    // Spawn Position of the Hero:
    // - startTile position if present
    // - randomTile position if startTile is not present
    // - (0,0) if no tiles are present
    Point spawnPos =
        Game.currentLevel()
            .flatMap(
                level ->
                    level
                        .startTile()
                        .map(Tile::position)
                        .or(() -> level.randomTile().map(Tile::position)))
            .orElse(new Point(0, 0));

    List<Coordinate> customPoints = new ArrayList<>();
    if (Game.currentLevel().orElse(null) instanceof DungeonLevel) {
      customPoints = Game.currentLevel().orElse(null).customPoints();
    }
    if (!(currentLevel instanceof DungeonLevel dunLevel)) {
      System.out.println("Current level is not a DungeonLevel. Cannot save.");
      return;
    }

    // Compress the layout of the current level by removing all lines that only contain 'S'
    String dunLayout = compressDungeonLayout(Game.currentLevel().orElse(null).printLevel());

    return designLabel
        + "\n"
        + spawnPos.x()
        + ","
        + spawnPos.y()
        + "\n"
        + customPointsString
        + "\n"
        + dunLayout;
  }
}
