package level.produs.chapter1;

import contrib.components.LeverComponent;
import contrib.hud.DialogUtils;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;

public class Chapter131Level extends BlocklyLevel {

  private DoorTile door;
  private LeverComponent switch1, switch2;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter131Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Chapter131");
  }

  @Override
  protected void onFirstTick() {
    DialogUtils.showTextPopup(
        "Oh nein, die Abkürzung ist versperrt. Jetzt muss ich den langen Weg nehmen. Wenn es doch nur eine Möglichkeit gäbe, die Strecke schnell zu schaffen.",
        "Kapitel 1: Ausbruch");
    Coordinate stone1C = customPoints().get(0);
    Coordinate stone2C = customPoints().get(1);
    Game.add(MiscFactory.stone(stone1C.toCenteredPoint()));
    Game.add(MiscFactory.stone(stone2C.toCenteredPoint()));
    door = (DoorTile) Game.tileAT(new Coordinate(0, 5));
    door.close();
  }

  @Override
  protected void onTick() {}
}
