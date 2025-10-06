package level.produs;

import client.Client;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This is the start of the game. It is designed to help players get comfortable with the Blockly
 * controls. In this level, the hero can only move and turn; no monsters are present.
 */
public class Level001 extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level001(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 1");
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

    popups.add(
        new TextPopUp(
            "Willkommen im Blockly Dungeon! Heute wirst du mir helfen, aus den Fängen des Bösen zu entkommen.",
            "Blockly Dungeon"));

    popups.add(
        new TextPopUp(
            "Ich bin "
                + Client.WIZARD_NAME
                + ", der Codemagier! Mit deiner Hilfe kann ich Zauber wirken – aber nur, wenn du die richtigen Codeblöcke im Browser benutzt. Komm, ich zeige dir, wie das geht!",
            "Blockly Dungeon"));

    popups.add(
        new TextPopUp(
            "Das hier ist der Code. Der Start-Block markiert den Anfang deines Algorithmus – also dort, wo dein Zauber beginnt."
                + "Tipp: Nutze die Escape-Tate (ESC) oben Links auf deiner Tatatur um Bilder zu schließen.",
            "Blockly Dungeon"));
    popups.add(new ImagePopUp("popups/level001/start_block.png"));

    popups.add(
        new TextPopUp(
            "In der Seitenleiste findest du verschiedene Kategorien. Je weiter wir kommen, desto mehr magische Fähigkeiten werden freigeschaltet! Behalte diese Liste also gut im Auge. "
                + "Klicke mit der linken Maustaste, um dir die Zauber einer Kategorie anzusehen.",
            "Blockly Dungeon"));
    popups.add(new ImagePopUp("/popups/level001/sidebar.png"));

    popups.add(
        new TextPopUp(
            "Hier siehst du meine Zauber – oder wie du sie nennen würdest: Code-Blöcke. Mit ihnen sagst du mir, was ich tun soll. "
                + "Ziehe einfach einen Block mit gedrückter linker Maustaste unter den Start-Block, um deinen ersten Zauber zu wirken!",
            "Blockly Dungeon"));
    popups.add(new ImagePopUp("/popups/level001/first_move.png"));
    popups.add(
        new TextPopUp(
            "Einige Zauber wirken nur in Kombination mit anderen. Das erkennst du an den kleinen Puzzle-Ausschnitten an der Seite.",
            "Blockly Dungeon"));
    popups.add(new ImagePopUp("/popups/level001/turn.png"));

    popups.add(
        new TextPopUp(
            "Kombiniere meine Zauber geschickt, um mich bis zum Ausgang zu führen. Gemeinsam können wir das Böse besiegen!",
            "Blockly Dungeon"));
    popups.add(new ImagePopUp("/popups/level001/turn_example.png"));

    popups.add(
        new TextPopUp(
            "Mit dem Start-Knopf kannst du mich auf meine Reise schicken. Hab keine Angst, Fehler zu machen – ich bin ein mächtiger Magier und lasse mich so leicht nicht unterkriegen! "
                + "Wenn etwas schiefgeht, starten wir das Level einfach noch einmal. "
                + "Und noch ein letzter Tipp: Mit der Taste 'P' kannst du dir das Tutorial jederzeit wieder anschauen.",
            "Blockly Dungeon"));
    popups.add(new ImagePopUp("/popups/level001/start.png"));
    popups.add(new ImagePopUp("/popups/level001/reset.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDirection(Direction.DOWN);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      showPopups();
      showText = false;
    }
  }

  @Override
  protected void onTick() {}
}
