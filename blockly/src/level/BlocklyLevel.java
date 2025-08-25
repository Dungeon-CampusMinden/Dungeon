package level;

import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This class is used to store the values from a parsed level file. It contains the layout (the
 * tiles), the design label, the hero start position and the custom points. This class is used in
 * the LevelParser.
 */
public abstract class BlocklyLevel extends DungeonLevel {

  private final Set<String> blockedBlocklyElements = new HashSet<>();
  private final DesignLabel designLabel;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   * @param name The name of the level.
   */
  public BlocklyLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      List<Coordinate> customPoints,
      String name) {
    super(layout, designLabel, customPoints, name);
    this.designLabel = designLabel;
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      onFirstTick();
    } else {
      onTick();
    }
  }

  /**
   * Adds a block to the list of blocked blockly elements. This is used to determine which blocks or
   * categories are not allowed to be used in the level.
   *
   * <p>Will be sent to the blockly frontend upon loading the level.
   *
   * @param block The name of the element to be added to the list of blocked blockly elements.
   */
  public void blockBlocklyElement(String... block) {
    blockedBlocklyElements.addAll(List.of(block));
  }

  /**
   * This returns the set of blockly elements that are blocked by the level. This is used to
   * determine which elements are not allowed to be used in the level.
   *
   * <p>Will be sent to the blockly frontend upon loading the level.
   *
   * @return A set of strings containing the names of the elements that are blocked.
   */
  public Set<String> blockedBlocklyElements() {
    return blockedBlocklyElements;
  }

  /**
   * Get the design of this level.
   *
   * @return The Design of this level.
   */
  public Optional<DesignLabel> designLabel() {
    return Optional.ofNullable(designLabel);
  }
}
