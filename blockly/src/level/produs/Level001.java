package level.produs;

import client.Client;
import com.badlogic.gdx.Input;
import core.configuration.KeyboardConfig;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import level.BlocklyLevel;
import level.LevelManagementUtils;

import java.util.Map;

/**
 * This is the start of the game. It is designed to help players get comfortable with the Blockly
 * controls. In this level, the player can only move and turn; no monsters are present.
 */
public class Level001 extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level001(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 1");
    this.blockBlocklyElement(
        // Richtungen
        "direction_up",
        "direction_down",
        "direction_here",
        // Kategorien
        "Inventar & Charakter",
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Schleife",
        "Bedingungen",
        "Sonstige");

    addPopup(
        new TextPopup(
            "Willkommen im Blockly Dungeon! Heute wirst du mir helfen, aus den Fängen des Bösen zu entkommen."
                + " Drücke "
                + Input.Keys.toString(KeyboardConfig.PAUSE.value())
                + " wenn ich mich wiederholen soll.",
            "Blockly Dungeon"));

    addPopup(
        new TextPopup(
            "Ich bin "
                + Client.WIZARD_NAME
                + ", der Codemagier! Mit deiner Hilfe kann ich Zauber wirken – aber nur, wenn du die richtigen Codeblöcke im Browser benutzt. Komm, ich zeige dir, wie das geht!",
            "Blockly Dungeon"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.playerViewDirection(Direction.DOWN);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      showPopups();
      showText = false;
    }
  }
}
