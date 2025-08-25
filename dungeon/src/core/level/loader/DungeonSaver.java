package core.level.loader;

import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.utils.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for saving the current state of the dungeon in the game. The "saving"
 * is done by printing the design label of the current level, the position of the hero, and the
 * layout of the current level to the console. This String can then be copied and pasted into a
 * .level file to be loaded later by the {@link DungeonLoader}.
 *
 * @see DungeonLoader
 */
public class DungeonSaver {

  /**
   * The saveCurrentDungeon method is responsible for saving the current state of the dungeon. It
   * does this by first getting the design label of the current level. Then it gets the position of
   * the hero in the game. After that, it compresses the layout of the current level by removing all
   * lines that only contain Empty Tiles. Finally, it concatenates all this information into a
   * single string and prints it.
   */
  public static void saveCurrentDungeon() {
    String designLabel =
        Game.currentLevel()
            .flatMap(level -> level.designLabel())
            .map(DesignLabel::name)
            .orElse(DesignLabel.DEFAULT.name());

    Point heroPos =
        Game.hero()
            .flatMap(hero -> hero.fetch(PositionComponent.class).map(PositionComponent::position))
            .orElse(new Point(0, 0));

    List<Coordinate> customPoints = new ArrayList<>();
    if (Game.currentLevel().orElse(null) instanceof DungeonLevel) {
      customPoints = Game.currentLevel().orElse(null).customPoints();
    }
    StringBuilder customPointsString = new StringBuilder();
    for (Coordinate customPoint : customPoints) {
      customPointsString.append(customPoint).append(";");
    }

    // Compress the layout of the current level by removing all lines that only contain 'S'
    String dunLayout = compressDungeonLayout(Game.currentLevel().orElse(null).printLevel());

    String result =
        designLabel
            + "\n"
            + heroPos.x()
            + ","
            + heroPos.y()
            + "\n"
            + customPointsString
            + "\n"
            + dunLayout;

    System.out.println(result);
  }

  /**
   * The compressDungeonLayout method takes a multi-line string as input and returns a string where
   * all lines containing only 'S' are removed. It does this by using the replaceAll method with a
   * regular expression that matches lines containing only 'S' and replaces them with an empty
   * string.
   *
   * @param layout The dungeon layout to compress.
   * @return The compressed dungeon layout.
   */
  private static String compressDungeonLayout(String layout) {
    return layout.replaceAll("(?m)^S+$\\n", "");
  }
}
