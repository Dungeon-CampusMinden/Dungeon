package core.level;

import contrib.entities.SignFactory;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.List;

public class InteractTestLevel extends DungeonLevel {

  public InteractTestLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "BlinkTestLevel");
  }

  @Override
  public void onFirstTick() {
    Point p = customPoints.get(0).toCenteredPoint();

    Entity sign =
        SignFactory.createSign(
            "Hallo! Das ist ein Interaktions-Test.", "Briefkasten-Ersatz", p, (entity, who) -> {});
    Game.add(sign);
  }

  @Override
  public void onTick() {}
}
