package portal.level;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;
import portal.util.AdvancedLevel;

public class IntroCube extends AdvancedLevel {

  public IntroCube(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "IntroCube");
  }

  @Override
  protected void onFirstTick() {}
}
