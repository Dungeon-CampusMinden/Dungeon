package level;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import level.utils.ITickable;

/** The tutorial level */
public class DevLevel00 extends DevDungeonLevel implements ITickable {

  public DevLevel00(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
  }

  @Override
  public void onTick(boolean isFirstTick) {

  }
}
