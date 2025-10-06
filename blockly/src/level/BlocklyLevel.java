package level;

import contrib.hud.DialogUtils;
import core.Game;
import core.System;
import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.IVoidFunction;
import java.util.*;
import systems.BlocklyCommandExecuteSystem;

/**
 * This class is used to store the values from a parsed level file. It contains the layout (the
 * tiles), the design label, the hero start position and the custom points. This class is used in
 * the LevelParser.
 */
public abstract class BlocklyLevel extends DungeonLevel {

  private final Set<String> blockedBlocklyElements = new HashSet<>();
  private final DesignLabel designLabel;

  /** List of Popups to display with {@link #showPopups()} */
  protected List<PopUp> popups = new ArrayList<>();

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

      Game.system(BlocklyCommandExecuteSystem.class, System::run);
    } else {
      onTick();
    }
  }

  /**
   * Shows each popup in {@link #popups} in the order they were added.
   *
   * <p>When a popup is closed, the next one in the list will open automatically.
   */
  protected void showPopups() {
    if (popups.isEmpty()) return;
    showNextPopup(0);
  }

  /**
   * Recursively shows popups one after another.
   *
   * @param index the index of the popup to show
   */
  private void showNextPopup(int index) {
    if (index >= popups.size()) return; // all popups shown

    PopUp current = popups.get(index);

    IVoidFunction onClose = () -> showNextPopup(index + 1);

    if (current instanceof TextPopUp textPopUp) {
      DialogUtils.showTextPopup(textPopUp.content(), textPopUp.title(), onClose);
    } else if (current instanceof ImagePopUp imagePopUp) {
      DialogUtils.showImagePopUp(imagePopUp.content(), onClose);
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

  /**
   * Represents a popup definition that can be queued and automatically shown at the start of a
   * level.
   */
  protected abstract class PopUp {
    private final String content;

    /**
     * Creates a new popup with the specified content.
     *
     * @param content the content to display when the popup is shown
     */
    public PopUp(String content) {
      this.content = content;
    }

    /**
     * Returns the content of the popup.
     *
     * @return the popup content
     */
    public String content() {
      return this.content;
    }
  }

  /** A popup that displays text content along with a title. */
  protected class TextPopUp extends PopUp {
    private final String title;

    /**
     * Creates a new text popup with the specified content and title.
     *
     * @param content the main text displayed in the popup
     * @param title the title shown at the top of the popup
     */
    public TextPopUp(String content, String title) {
      super(content);
      this.title = title;
    }

    /**
     * Returns the title of the popup.
     *
     * @return the popup title
     */
    public String title() {
      return title;
    }
  }

  /** A popup that displays an image. */
  protected class ImagePopUp extends PopUp {

    /**
     * Creates a new image popup with the specified image path or identifier.
     *
     * @param content the image path to display
     */
    public ImagePopUp(String content) {
      super(content);
    }
  }
}
