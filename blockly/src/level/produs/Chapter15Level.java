package level.produs;

import static level.LevelManagementUtils.cameraFocusOn;

import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonsterFactory;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;

public class Chapter15Level extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter15Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 5");
  }

  @Override
  protected void onFirstTick() {
    cameraFocusOn(new Coordinate(7, 6));

    Coordinate stone1C = customPoints().get(1);
    Coordinate stone2C = customPoints().get(5);
    Coordinate m1C = customPoints().get(0);
    Coordinate m2C = customPoints().get(2);
    Coordinate m3C = customPoints().get(3);
    Coordinate m4C = customPoints().get(4);

    Game.add(MiscFactory.stone(stone1C.toCenteredPoint()));
    Game.add(MiscFactory.stone(stone2C.toCenteredPoint()));
    // TODO replace with guards
    Game.add(BlocklyMonsterFactory.guard(m1C, PositionComponent.Direction.LEFT, 5));
    Game.add(BlocklyMonsterFactory.guard(m2C, PositionComponent.Direction.RIGHT, 5));
    Game.add(BlocklyMonsterFactory.guard(m3C, PositionComponent.Direction.UP, 5));
    Game.add(BlocklyMonsterFactory.guard(m4C, PositionComponent.Direction.UP, 5));
  }

  @Override
  protected void onTick() {}
}
