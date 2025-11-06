package level;

import client.Client;
import contrib.hud.DialogUtils;
import core.Game;
import core.System;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.IVoidFunction;
import core.utils.Point;
import java.util.*;
import systems.BlocklyCommandExecuteSystem;

/**
 * This class is used to store the values from a parsed level file. It contains the layout (the
 * tiles), the design label, the player start position and the custom points. This class is used in
 * the LevelParser.
 */
public abstract class BlocklyLevel extends DungeonLevel {

  private final Set<String> blockedBlocklyElements = new HashSet<>();
  private final DesignLabel designLabel;

  /**
   * List of Popups to display with {@link #showPopups()} if the game is controlled via the Blockly
   * Web UI.
   */
  private List<Popup> webPopups = new ArrayList<>();

  /**
   * List of Popups to display with {@link #showPopups()} if the game is controlled via the
   * Code-API.
   */
  private List<Popup> codePopups = new ArrayList<>();

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   * @param name The name of the level.
   */
  public BlocklyLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      String name) {
    super(layout, designLabel, namedPoints, name);
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
   * Shows each popup in {@link #webPopups} in the order they were added.
   *
   * <p>When a popup is closed, the next one in the list will open automatically.
   */
  public void showPopups() {
    if (Client.runInWeb) {
      if (webPopups.isEmpty()) return;
      showNextPopup(webPopups, 0);
    } else {
      if (codePopups.isEmpty()) return;
      showNextPopup(codePopups, 0);
    }
  }

  /**
   * Recursively shows popups one after another.
   *
   * @param popups Popups to show
   * @param index the index of the popup to show
   */
  private void showNextPopup(List<Popup> popups, int index) {
    if (index >= popups.size()) return; // all popups shown

    Popup current = popups.get(index);

    IVoidFunction onClose = () -> showNextPopup(popups, index + 1);

    if (current instanceof TextPopup textPopUp) {
      DialogUtils.showTextPopup(textPopUp.content(), textPopUp.title(), onClose);
    } else if (current instanceof ImagePopup imagePopUp) {
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
   * Adds the given popup to the collection of web popups.
   *
   * @param popup the {@link Popup} instance to add to the web popups list
   */
  protected void addWebPopup(Popup popup) {
    this.webPopups.add(popup);
  }

  /**
   * Adds the given popup to the collection of code popups.
   *
   * @param popup the {@link Popup} instance to add to the code popups list
   */
  protected void addCodePopup(Popup popup) {
    this.codePopups.add(popup);
  }

  /**
   * Adds the given popup to both the code and web popup collections.
   *
   * @param popup the {@link Popup} instance to add to both lists
   */
  protected void addPopup(Popup popup) {
    addCodePopup(popup);
    addWebPopup(popup);
  }

  /**
   * Represents a popup definition that can be queued and automatically shown at the start of a
   * level.
   */
  protected abstract class Popup {
    private final String content;

    /**
     * Creates a new popup with the specified content.
     *
     * @param content the content to display when the popup is shown
     */
    public Popup(String content) {
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
  protected class TextPopup extends Popup {
    private final String title;

    /**
     * Creates a new text popup with the specified content and title.
     *
     * @param content the main text displayed in the popup
     * @param title the title shown at the top of the popup
     */
    public TextPopup(String content, String title) {
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
  protected class ImagePopup extends Popup {

    /**
     * Creates a new image popup with the specified image path or identifier.
     *
     * @param content the image path to display
     */
    public ImagePopup(String content) {
      super(content);
    }
  }
}
