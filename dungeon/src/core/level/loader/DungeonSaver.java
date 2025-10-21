package core.level.loader;

import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.utils.ClipboardUtil;
import core.utils.Point;
import java.awt.*;

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
    ILevel currentLevel = Game.currentLevel().orElse(null);
    if (currentLevel == null) {
      return;
    }
    if (!(currentLevel instanceof DungeonLevel dunLevel)) {
      System.out.println("Current level is not a DungeonLevel. Cannot save.");
      return;
    }

    String designLabel =
        dunLevel.designLabel().map(DesignLabel::name).orElse(DesignLabel.DEFAULT.name());

    Point heroPos =
        Game.hero()
            .flatMap(hero -> hero.fetch(PositionComponent.class).map(PositionComponent::position))
            .orElse(new Point(0, 0));
    String heroPosString = heroPos.x() + "," + heroPos.y();

    String customPointsString = LevelParser.v2SerializeNamedPoints(dunLevel.namedPoints());
    String decorations = LevelParser.v2SerializeDecorationList(dunLevel.decorations());
    String dunLayout = LevelParser.v2SerializeLevelLayout(dunLevel.layout());

    StringBuilder result = new StringBuilder();
    result.append(designLabel).append("\n");
    result.append(heroPosString).append("\n");
    result.append(customPointsString).append("\n");
    result.append(decorations).append("\n");
    result.append(dunLayout);
    String output = result.toString();

    System.out.println(output);
    ClipboardUtil.copyToClipboard(output);
  }
}
