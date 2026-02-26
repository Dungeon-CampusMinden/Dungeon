package level.produs;

import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level builds on the previous one: fireball scrolls must be collected to defeat monsters.
 * Now, clever positioning is essential to succeed.
 */
public class Level011 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level011(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 11");
    this.blockBlocklyElement(
        // Schleifen
        "while_loop",
        // Inventar und Charakter
        "drop_item",
        "Items",
        "wait",
        // Kategorien
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Bedingungen",
        "Sonstige");
    addCodePopup(new ImagePopup("popups/overview1.jpg"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(8, 6));
    LevelManagementUtils.playerViewDirection(Direction.RIGHT);
    Game.add(MiscFactory.fireballScroll(getPoint(0)));
    Game.add(MiscFactory.fireballScroll(getPoint(1)));
    Game.add(MiscFactory.fireballScroll(getPoint(2)));
    Game.add(MiscFactory.fireballScroll(getPoint(3)));
    Game.add(MiscFactory.fireballScroll(getPoint(4)));

    BlocklyMonster.Builder guardBuilder = BlocklyMonster.GUARD.builder().attackRange(5).addToGame();
    guardBuilder.viewDirection(Direction.DOWN);
    guardBuilder.build(getPoint(5));
    guardBuilder.build(getPoint(8));
    guardBuilder.build(getPoint(9));
    guardBuilder.viewDirection(Direction.RIGHT);
    guardBuilder.build(getPoint(6));
    guardBuilder.build(getPoint(7));

    if (showText) {
      showPopups();
      showText = false;
    }
  }

  @Override
  protected void onTick() {}
}
