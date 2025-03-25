package level.produs.chapter1;

import contrib.components.LeverComponent;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;

public class Chapter13Level extends BlocklyLevel {

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
  public Chapter13Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Chapter13");
  }

  @Override
  protected void onFirstTick() {
    Coordinate stone1C = customPoints().get(0);
    Coordinate stone2C = customPoints().get(1);
    Coordinate switch1C = customPoints().get(2);
    Coordinate switch2C = customPoints().get(3);
    Game.add(MiscFactory.stone(stone1C.toCenteredPoint()));
    Game.add(MiscFactory.stone(stone2C.toCenteredPoint()));
    Entity s1 = MiscFactory.pressurePlate(switch1C.toCenteredPoint());
    Entity s2 = MiscFactory.pressurePlate(switch2C.toCenteredPoint());
    Game.add(s1);
    Game.add(s2);
    switch1 = s1.fetch(LeverComponent.class).get();
    switch2 = s2.fetch(LeverComponent.class).get();
    door = (DoorTile) Game.tileAT(new Coordinate(5, 12));
    door.close();
  }

  @Override
  protected void onTick() {
    if (switch1.isOn() && switch2.isOn()) door.open();
    else door.close();
  }
}
