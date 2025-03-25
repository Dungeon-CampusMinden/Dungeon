package level.produs.chapter1;

import core.Entity;
import core.Game;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonsterFactory;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;

public class Chapter14Level extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter14Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Chapter14");
  }

  @Override
  protected void onFirstTick() {
    Game.hero().get().remove(CameraComponent.class);
    Entity focusPoint = new Entity();
    focusPoint.add(new PositionComponent(7, 6));
    focusPoint.add(new CameraComponent());
    Game.add(focusPoint);

    Coordinate stone1C = customPoints().get(1);
    Coordinate stone2C = customPoints().get(5);
    Coordinate m1C = customPoints().get(0);
    Coordinate m2C = customPoints().get(2);
    Coordinate m3C = customPoints().get(3);
    Coordinate m4C = customPoints().get(4);

    Game.add(MiscFactory.stone(stone1C.toCenteredPoint()));
    Game.add(MiscFactory.stone(stone2C.toCenteredPoint()));
    // TODO replace with guards
    Game.add(BlocklyMonsterFactory.guard(m1C, "Monster 1", PositionComponent.Direction.LEFT));
    Game.add(BlocklyMonsterFactory.guard(m2C, "Monster 2", PositionComponent.Direction.RIGHT));
    Game.add(BlocklyMonsterFactory.guard(m3C, "Monster 3", PositionComponent.Direction.UP));
    Game.add(BlocklyMonsterFactory.guard(m4C, "Monster 4", PositionComponent.Direction.UP));
  }

  @Override
  protected void onTick() {}
}
