package contrib.utils;

import core.level.Tile;
import core.level.utils.LevelElement;

/**
 * A utility class to paint a checker pattern on tiles in a level layout.
 *
 * <p>This class provides a method to apply a checker pattern to tiles that are marked with specific
 * level elements. The pattern is applied by alternating colors based on the tile's position in the
 * layout.
 *
 * @see #paintCheckerPattern(Tile[][])
 */
public class CheckPatternPainter {

  /**
   * The color used for the checker pattern. This color is applied to the tiles that are marked with
   * the specified level elements.
   *
   * @see #LEVEL_ELEMENTS_TO_PAINT
   */
  private static final int CHECKER_PATTERN_COLOR = 0xffffffcc; // slightly darker

  /** The level elements that will be painted with the checker pattern. */
  private static final LevelElement[] LEVEL_ELEMENTS_TO_PAINT = {
    LevelElement.FLOOR,
  };

  /**
   * Paints a checker pattern on the given layout of tiles. The pattern is applied to the tiles that
   * are marked with the specified level elements.
   *
   * @param layout The 2D array of tiles to paint.
   */
  public static void paintCheckerPattern(Tile[][] layout) {
    for (int i = 0; i < layout.length; i++) {
      for (int j = 0; j < layout[i].length; j++) {
        if ((i + j) % 2 == 0) {
          if (isLevelElementToPaint(layout[i][j].levelElement())) {
            layout[i][j].tintColor(CHECKER_PATTERN_COLOR);
          }
        }
      }
    }
  }

  private static boolean isLevelElementToPaint(LevelElement element) {
    for (LevelElement levelElement : LEVEL_ELEMENTS_TO_PAINT) {
      if (levelElement == element) {
        return true;
      }
    }
    return false;
  }
}
